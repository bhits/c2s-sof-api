package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.validation.FhirValidator;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.AbstractCareTeamDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.ConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.ConsentMedicalInfoType;
import gov.samhsa.c2s.c2ssofapi.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.GeneralConsentRelatedFieldDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PdfDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.ReferenceDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.ValueSetDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.ConsentPdfGenerationException;
import gov.samhsa.c2s.c2ssofapi.service.exception.DuplicateResourceFoundException;
import gov.samhsa.c2s.c2ssofapi.service.exception.NoDataFoundException;
import gov.samhsa.c2s.c2ssofapi.service.exception.PreconditionFailedException;
import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;
import gov.samhsa.c2s.c2ssofapi.service.pdf.ConsentPdfGenerator;
import gov.samhsa.c2s.c2ssofapi.service.pdf.ConsentRevocationPdfGenerator;
import gov.samhsa.c2s.c2ssofapi.service.util.FhirDtoUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.FhirUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CareTeam;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.codesystems.V3ActReason;
import org.hl7.fhir.exceptions.FHIRException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.uhn.fhir.rest.api.Constants.PARAM_LASTUPDATED;
import static java.util.stream.Collectors.toList;


@Service
@Slf4j
public class ConsentServiceImpl implements ConsentService {
    private static final String INFORMANT_CODE = "INF";
    private static final String INFORMANT_RECIPIENT_CODE = "IRCP";
    private static final String PSEUDO_ORGANIZATION_NAME = "Omnibus Care Plan (SAMHSA)";
    private static final String PSEUDO_ORGANIZATION_TAX_ID = "530196960";
    private static final String CONTENTTYPE = "application/pdf";
    private static final Boolean operatedByPatient = true;


    private final IGenericClient fhirClient;
    private final LookUpService lookUpService;
    private final ConfigProperties configProperties;
    private final ModelMapper modelMapper;
    private final ConsentPdfGenerator consentPdfGenerator;
    private final ConsentRevocationPdfGenerator consentRevocationPdfGenerator;

    private final PatientService patientService;

    private final FhirValidator fhirValidator;


    @Autowired
    public ConsentServiceImpl(ModelMapper modelMapper,
                              IGenericClient fhirClient,
                              LookUpService lookUpService,
                              ConfigProperties configProperties,
                              ConsentPdfGenerator consentPdfGenerator,
                              ConsentRevocationPdfGenerator consentRevocationPdfGenerator,
                              PatientService patientService, FhirValidator fhirValidator) {
        this.modelMapper = modelMapper;
        this.fhirClient = fhirClient;
        this.lookUpService = lookUpService;
        this.configProperties = configProperties;
        this.consentPdfGenerator = consentPdfGenerator;
        this.consentRevocationPdfGenerator = consentRevocationPdfGenerator;
        this.patientService = patientService;
        this.fhirValidator = fhirValidator;
    }

    @Override
    public PageDto<DetailedConsentDto> getConsents(Optional<String> patient, Optional<String> practitioner, Optional<String> status, Optional<Boolean> generalDesignation, Optional<Integer> pageNumber, Optional<Integer> pageSize) {

        int numberOfConsentsPerPage = PaginationUtil.getValidPageSize(configProperties, pageSize, ResourceType.Consent.name());

        // Generate the Query Based on Input Variables
        IQuery consentQueryWithCareTeam = getConsentIQuery(patient, practitioner, status, generalDesignation);

        IQuery finalConsentQueryWithCareTeam = consentQueryWithCareTeam;

        if (!status.isPresent()) {
            practitioner.ifPresent(pr -> {
                if (!getCareTeamIdsFromPractitioner(pr).isEmpty()) {
                    finalConsentQueryWithCareTeam.where(new ReferenceClientParam("actor").hasAnyOfIds(getCareTeamIdsFromPractitioner(pr)));
                }
            });
        }

        // Disable caching to get latest data
        consentQueryWithCareTeam = FhirUtil.setNoCacheControlDirective(finalConsentQueryWithCareTeam);

        Bundle consentBundleWithCareTeam = (Bundle) consentQueryWithCareTeam.returnBundle(Bundle.class).execute();

        List<Bundle.BundleEntryComponent> consentBundleEntryWithCareTeam = FhirUtil.getAllBundleComponentsAsList(consentBundleWithCareTeam, Optional.of(numberOfConsentsPerPage), fhirClient, configProperties);

        List<Bundle.BundleEntryComponent> bundleEntryComponentList = new ArrayList<>();
        if (!practitioner.isPresent()) {
            bundleEntryComponentList.addAll(consentBundleEntryWithCareTeam);
        }
        //Get consent according to the practitioner.
        practitioner.ifPresent(pr -> {
            Bundle bundle = (Bundle) getConsentIQuery(patient, Optional.empty(), status, generalDesignation).returnBundle(Bundle.class).execute();
            List<Bundle.BundleEntryComponent> consents = FhirUtil.getAllBundleComponentsAsList(bundle, Optional.ofNullable(numberOfConsentsPerPage), fhirClient, configProperties)
                    .stream().filter(cs -> {
                                Consent consent = (Consent) cs.getResource();
                                return !consent.getActor().stream()
                                        .filter(ac -> ac.getReference().getReference()
                                                .split("/")[1].equalsIgnoreCase(pr))
                                        .collect(toList()).isEmpty();
                            }
                    ).collect(toList());
            bundleEntryComponentList.addAll(consents);
        });

        bundleEntryComponentList.stream().distinct().collect(toList());


        // Map to DTO
        List<DetailedConsentDto> consentDtosList = bundleEntryComponentList.stream().map(this::convertConsentBundleEntryToConsentDto).collect(toList());

        return (PageDto<DetailedConsentDto>) PaginationUtil.applyPaginationForCustomArrayList(consentDtosList, numberOfConsentsPerPage, pageNumber, false);

    }

    @Override
    public DetailedConsentDto getConsentsById(String consentId) {
        log.info("Searching for consentId: " + consentId);
        IQuery consentQuery = fhirClient.search().forResource(Consent.class)
                .where(new TokenClientParam("_id").exactly().code(consentId.trim()));

        consentQuery = FhirUtil.setNoCacheControlDirective(consentQuery);

        Bundle consentBundle = (Bundle) consentQuery.returnBundle(Bundle.class)
                .execute();

        if (consentBundle == null || consentBundle.getEntry().isEmpty()) {
            log.info("No consent was found for the given consentId:" + consentId);
            throw new ResourceNotFoundException("No consent was found for the given consent ID:" + consentId);
        }

        log.info("FHIR consent bundle retrieved from FHIR server successfully for consent ID:" + consentId);

        Bundle.BundleEntryComponent retrievedConsent = consentBundle.getEntry().get(0);
        return convertConsentBundleEntryToConsentDto(retrievedConsent);
    }

    @Override
    public GeneralConsentRelatedFieldDto getGeneralConsentRelatedFields(String patient) {
        GeneralConsentRelatedFieldDto generalConsentRelatedFieldDto = new GeneralConsentRelatedFieldDto();

        //Adding To careTeams
        Bundle careTeamBundle = fhirClient.search().forResource(CareTeam.class)
                .where(new ReferenceClientParam("subject").hasId(patient))
                .returnBundle(Bundle.class).execute();

        if (!careTeamBundle.getEntry().isEmpty()) {
            List<ReferenceDto> toActors = careTeamBundle.getEntry().stream().map(careTeamEntry -> {
                CareTeam careTeam = (CareTeam) careTeamEntry.getResource();
                return convertCareTeamToReferenceDto(careTeam);
            }).collect(toList());

            generalConsentRelatedFieldDto.setToActors(toActors);

            //Adding from careTeams
            Bundle organizationBundle = getPseudoOrganization();

            organizationBundle.getEntry().stream().findAny().ifPresent(entry -> {
                Organization organization = (Organization) entry.getResource();
                ReferenceDto referenceDto = new ReferenceDto();
                referenceDto.setReference("Organization/" + organization.getIdElement().getIdPart());
                referenceDto.setDisplay(PSEUDO_ORGANIZATION_NAME);
                generalConsentRelatedFieldDto.setFromActors(Collections.singletonList(referenceDto));
            });

            generalConsentRelatedFieldDto.setPurposeOfUse(FhirDtoUtil.convertCodeToValueSetDto(V3ActReason.TREAT.toCode(), lookUpService.getPurposeOfUse()));
            generalConsentRelatedFieldDto.setMedicalInformation(lookUpService.getSecurityLabel());

        } else {
            throw new ResourceNotFoundException("No care teams are present.");
        }
        return generalConsentRelatedFieldDto;
    }


    @Override
    public void createConsent(ConsentDto consentDto) {
        //Create Consent
        Bundle associatedCareTeam = fhirClient.search().forResource(CareTeam.class).where(new ReferenceClientParam("patient").hasId(consentDto.getPatient().getReference()))
                .returnBundle(Bundle.class).execute();
        if (consentDto.isGeneralDesignation()) {
            if (!associatedCareTeam.getEntry().isEmpty()) {
                if (!isDuplicate(consentDto, Optional.empty())) {
                    Consent consent = consentDtoToConsent(Optional.empty(), consentDto);
                    //Validate
                    FhirUtil.validateFhirResource(fhirValidator, consent, Optional.empty(), ResourceType.Consent.name(), "Create Consent");

                    fhirClient.create().resource(consent).execute();
                } else {
                    throw new DuplicateResourceFoundException("This patient already has a general designation consent.");
                }
            } else {
                throw new PreconditionFailedException("No care team members for this patient.");
            }
        } else {
            Consent consent = consentDtoToConsent(Optional.empty(), consentDto);
            //Validate
            FhirUtil.validateFhirResource(fhirValidator, consent, Optional.empty(), ResourceType.Consent.name(), "Create Consent");

            fhirClient.create().resource(consent).execute();
        }
    }

    @Override
    public void updateConsent(String consentId, ConsentDto consentDto) {
        //Update Consent
        if (!isDuplicate(consentDto, Optional.of(consentId))) {
            Consent consent = consentDtoToConsent(Optional.of(consentId), consentDto);
            consent.setId(consentId);

            //Validate
            FhirUtil.validateFhirResource(fhirValidator, consent, Optional.of(consentId), ResourceType.Consent.name(), "Update Consent");

            fhirClient.update().resource(consent).execute();
        } else {
            throw new DuplicateResourceFoundException("This patient already has a general designation consent.");
        }
    }

    @Override
    public PageDto<AbstractCareTeamDto> getActors(Optional<String> patientId, Optional<String> name, Optional<String> actorType, Optional<List<String>> actorsAlreadyAssigned, Optional<Integer> pageNumber, Optional<Integer> pageSize) {
        int numberOfActorsPerPage = PaginationUtil.getValidPageSize(configProperties, pageSize, ResourceType.Consent.name());

        //Getting List of practitioners
        List<AbstractCareTeamDto> abstractCareTeamDtoList = FhirUtil.getPractitionerActors(patientId, name, Optional.empty(), Optional.empty(), fhirClient, configProperties);

        //Add organizations
        abstractCareTeamDtoList.addAll(FhirUtil.getOrganizationActors(patientId, name, Optional.empty(), Optional.empty(), fhirClient, configProperties));

        //Add related Person
        abstractCareTeamDtoList.addAll(FhirUtil.getRelatedPersonActors(patientId, name, Optional.empty(), Optional.empty(), fhirClient, configProperties));

        actorType.ifPresent(type -> abstractCareTeamDtoList.removeIf(actors -> !actors.getCareTeamType().toString().equalsIgnoreCase(type)));
        actorsAlreadyAssigned.ifPresent(actorsAlreadyPresent -> abstractCareTeamDtoList.removeIf(abstractCareTeamDto -> actorsAlreadyPresent.contains(abstractCareTeamDto.getId())));

        return (PageDto<AbstractCareTeamDto>) PaginationUtil.applyPaginationForCustomArrayList(abstractCareTeamDtoList, numberOfActorsPerPage, pageNumber, false);
    }


    private DetailedConsentDto convertConsentBundleEntryToConsentDto(Bundle.BundleEntryComponent fhirConsentDtoModel) {
        ConsentDto consentDto = modelMapper.map(fhirConsentDtoModel.getResource(), ConsentDto.class);

        consentDto.getFromActor().stream().filter(member -> member.getDisplay().equalsIgnoreCase("Omnibus Care Plan (SAMHSA)")).map(member -> true).forEach(consentDto::setGeneralDesignation);

        Consent consent = (Consent) fhirConsentDtoModel.getResource();

        //setting medical info type
        if (consentDto.getMedicalInformation() != null) {
            int totalMedicalInfo = lookUpService.getSecurityLabel() != null ?
                    lookUpService.getSecurityLabel().size() : 0;
            if (consentDto.getMedicalInformation().size() < lookUpService.getSecurityLabel().size()) {
                consentDto.setConsentMedicalInfoType(ConsentMedicalInfoType.SHARE_SPECIFIC);
            } else {
                consentDto.setConsentMedicalInfoType(ConsentMedicalInfoType.SHARE_ALL);
            }
        }

        DetailedConsentDto detailedConsentDto = convertConsentDtoToDetailedConsentDto(consentDto);

        try {
            if (consent.hasSourceAttachment() && !consentDto.getStatus().equalsIgnoreCase("draft")) {
                detailedConsentDto.setSourceAttachment(consent.getSourceAttachment().getData());
            } else if (consentDto.getStatus().equalsIgnoreCase("draft")) {
                String patientID = consentDto.getPatient().getReference().replace("Patient/", "");
                PatientDto patientDto = patientService.getPatientById(patientID, null);
                log.info("Generating consent PDF");
                byte[] pdfBytes = consentPdfGenerator.generateConsentPdf(detailedConsentDto, patientDto, operatedByPatient, Optional.empty());
                detailedConsentDto.setSourceAttachment(pdfBytes);
            }

        } catch (FHIRException | IOException e) {
            log.error("No Consent document found");
            throw new NoDataFoundException("No Consent document found");
        }

        return detailedConsentDto;
    }

    private IQuery getConsentIQuery(Optional<String> patient, Optional<String> practitioner, Optional<String> status, Optional<Boolean> generalDesignation) {
        IQuery iQuery = fhirClient.search().forResource(Consent.class).sort().descending(PARAM_LASTUPDATED);

        //Query the status.
        if (status.isPresent()) {
            iQuery.where(new TokenClientParam("status").exactly().code("active"));
        } else {
            //query with practitioner.
            practitioner.ifPresent(pr -> {
                if (!getCareTeamIdsFromPractitioner(pr).isEmpty()) {
                    iQuery.where(new ReferenceClientParam("actor").hasAnyOfIds(getCareTeamIdsFromPractitioner(pr)));
                }
            });

            //query with patient.
            patient.ifPresent(pt -> iQuery.where(new ReferenceClientParam("patient").hasId(pt)));

            //Query with general designation.
            generalDesignation.ifPresent(gd -> {
                if (gd) {
                    String pseudoOrgId = getPseudoOrganization().getEntry().stream().findFirst().map(pseudoOrgEntry -> {
                        Organization organization = (Organization) pseudoOrgEntry.getResource();
                        return organization.getIdElement().getIdPart();
                    }).get();
                    iQuery.where(new ReferenceClientParam("actor").hasId(pseudoOrgId));
                }
            });

        }
        return iQuery;
    }

    @Override
    public void attestConsent(String consentId) {

        Consent consent = fhirClient.read().resource(Consent.class).withId(consentId.trim()).execute();
        consent.setStatus(Consent.ConsentState.ACTIVE);

        DetailedConsentDto detailedConsentDto = getConsentsById(consentId);
        detailedConsentDto.setStatus("Active");

        String patientID = detailedConsentDto.getPatient().getReference().replace("Patient/", "");
        PatientDto patientDto = patientService.getPatientById(patientID, null);


        try {
            log.info("Updating consent: Generating the attested PDF");
            byte[] pdfBytes = consentPdfGenerator.generateConsentPdf(detailedConsentDto, patientDto, operatedByPatient, Optional.empty());
            consent.setSource(addAttachment(pdfBytes));

        } catch (IOException e) {
            throw new ConsentPdfGenerationException(e);
        }
        //consent.getSourceAttachment().getData();
        log.info("Updating consent: Saving the consent into the FHIR server.");
        //Validate
        FhirUtil.validateFhirResource(fhirValidator, consent, Optional.empty(), ResourceType.Consent.name(), "Attest Consent");

        fhirClient.update().resource(consent).execute();
    }

    @Override
    public void revokeConsent(String consentId) {

        Consent consent = fhirClient.read().resource(Consent.class).withId(consentId.trim()).execute();
        consent.setStatus(Consent.ConsentState.INACTIVE);

        DetailedConsentDto detailedConsentDto = getConsentsById(consentId);
        detailedConsentDto.setStatus("Inactive");

        String patientID = detailedConsentDto.getPatient().getReference().replace("Patient/", "");
        PatientDto patientDto = patientService.getPatientById(patientID, null);

        try {
            log.info("Updating consent: Generating the revocation PDF");
            byte[] pdfBytes = consentRevocationPdfGenerator.generateConsentRevocationPdf(detailedConsentDto, patientDto, operatedByPatient, Optional.empty());
            consent.setSource(addAttachment(pdfBytes));

        } catch (IOException e) {
            throw new ConsentPdfGenerationException(e);
        }
        //consent.getSourceAttachment().getData();
        log.info("Updating consent: Saving the consent into the FHIR server.");
        //Validate
        FhirUtil.validateFhirResource(fhirValidator, consent, Optional.empty(), ResourceType.Consent.name(), "Attest Consent");
        fhirClient.update().resource(consent).execute();
    }

    private Attachment addAttachment(byte[] pdfBytes) {
        Attachment attachment = new Attachment();
        attachment.setContentType(CONTENTTYPE);
        attachment.setData(pdfBytes);
        return attachment;
    }


    @Override
    public PdfDto createConsentPdf(String consentId) {
        DetailedConsentDto detailedConsentDto = getConsentsById(consentId);
        String patientID = detailedConsentDto.getPatient().getReference().replace("Patient/", "");
        PatientDto patientDto = patientService.getPatientById(patientID, null);

        try {
            log.info("Generating consent PDF");
            byte[] pdfBytes = consentPdfGenerator.generateConsentPdf(detailedConsentDto, patientDto, operatedByPatient, Optional.empty());
            return new PdfDto(pdfBytes);

        } catch (IOException e) {
            throw new ConsentPdfGenerationException(e);
        }
    }


    private Consent consentDtoToConsent(Optional<String> consentId, ConsentDto consentDto) {
        Consent consent = new Consent();
        if (consentDto.getPeriod() != null) {
            Period period = new Period();
            period.setStart((consentDto.getPeriod().getStart() != null) ? java.sql.Date.valueOf(consentDto.getPeriod().getStart()) : null);
            period.setEnd((consentDto.getPeriod().getEnd() != null) ? java.sql.Date.valueOf(consentDto.getPeriod().getEnd()) : null);
            consent.setPeriod(period);
        }

        consent.setPatient(FhirDtoUtil.mapReferenceDtoToReference(consentDto.getPatient()));

        if (!consentDto.getCategory().isEmpty() && consentDto.getCategory() != null) {
            List<CodeableConcept> categories = consentDto.getCategory().stream()
                    .map(FhirDtoUtil::convertValuesetDtoToCodeableConcept)
                    .collect(toList());
            consent.setCategory(categories);
        }

        if (consentDto.getDateTime() != null) {
            consent.setDateTime(java.sql.Date.valueOf(consentDto.getDateTime()));
        } else {
            consent.setDateTime(java.sql.Date.valueOf(LocalDate.now()));
        }

        if (!consentDto.getPurpose().isEmpty() && consentDto.getPurpose() != null) {
            List<Coding> purposes = consentDto.getPurpose().stream().map(purpose -> {
                Coding coding = new Coding();
                coding.setDisplay((purpose.getDisplay() != null && !purpose.getDisplay().isEmpty()) ? purpose.getDisplay() : null)
                        .setCode((purpose.getCode() != null && !purpose.getCode().isEmpty()) ? purpose.getCode() : null)
                        .setSystem((purpose.getSystem() != null && !purpose.getSystem().isEmpty()) ? purpose.getSystem() : null);
                return coding;
            }).collect(toList());

            consent.setPurpose(purposes);
        }

        if (consentDto.getStatus() != null) {
            if (consentDto.getStatus() != null) {
                try {
                    consent.setStatus(Consent.ConsentState.fromCode(consentDto.getStatus()));
                } catch (FHIRException e) {
                    throw new ResourceNotFoundException("Invalid consent status found.");
                }
            }
        }

        //Setting identifier
        if (!consentId.isPresent()) {
            Identifier identifier = new Identifier();
            identifier.setValue(UUID.randomUUID().toString());
            identifier.setSystem(configProperties.getConsent().getIdentifierSystem());
            consent.setIdentifier(identifier);
        } else if (consentDto.getIdentifier() != null) {
            Identifier identifier = new Identifier();
            identifier.setValue(consentDto.getIdentifier().getValue());
            identifier.setSystem(consentDto.getIdentifier().getSystem());
            consent.setIdentifier(identifier);
        }


        List<Consent.ConsentActorComponent> actors = new ArrayList<>();

        //Getting pseudo organization
        Bundle organizationBundle = getPseudoOrganization();

        organizationBundle.getEntry().stream().findAny().ifPresent(entry -> {
            Organization organization = (Organization) entry.getResource();
            ReferenceDto referenceDto = new ReferenceDto();
            referenceDto.setReference("Organization/" + organization.getIdElement().getIdPart());
            referenceDto.setDisplay(PSEUDO_ORGANIZATION_NAME);
            consent.setOrganization(Collections.singletonList(FhirDtoUtil.mapReferenceDtoToReference(referenceDto)));

            if (consentDto.isGeneralDesignation()) {
                Consent.ConsentActorComponent fromActor = new Consent.ConsentActorComponent();
                fromActor.setReference(FhirDtoUtil.mapReferenceDtoToReference(referenceDto))
                        .setRole(FhirDtoUtil.convertValuesetDtoToCodeableConcept(FhirDtoUtil.convertCodeToValueSetDto(INFORMANT_CODE, lookUpService.getSecurityRole())));
                actors.add(fromActor);
            }
        });

        if (consentDto.isGeneralDesignation()) {
            //Adding To careTeams
            Bundle careTeamBundle = fhirClient.search().forResource(CareTeam.class)
                    .where(new ReferenceClientParam("subject").hasId(consentDto.getPatient().getReference()))
                    .returnBundle(Bundle.class).execute();

            careTeamBundle.getEntry().stream().map(careTeamEntry -> (CareTeam) careTeamEntry.getResource()).map(careTeam -> convertCareTeamToActor(careTeam, FhirDtoUtil.convertCodeToValueSetDto(INFORMANT_RECIPIENT_CODE, lookUpService
                    .getSecurityRole()))).forEach(actors::add);
            consent.setActor(actors);
        } else {
            List<Consent.ConsentActorComponent> fromActors = consentDto.getFromActor().stream().map(fromActor -> {
                Consent.ConsentActorComponent from = new Consent.ConsentActorComponent();
                from.setReference(FhirDtoUtil.mapReferenceDtoToReference(fromActor)).setRole(FhirDtoUtil.convertValuesetDtoToCodeableConcept(FhirDtoUtil.convertCodeToValueSetDto(INFORMANT_CODE, lookUpService.getSecurityRole())));
                return from;
            }).collect(toList());

            List<Consent.ConsentActorComponent> toActors = consentDto.getToActor().stream().map(toActor -> {
                Consent.ConsentActorComponent to = new Consent.ConsentActorComponent();
                to.setReference(FhirDtoUtil.mapReferenceDtoToReference(toActor)).setRole(FhirDtoUtil.convertValuesetDtoToCodeableConcept(FhirDtoUtil.convertCodeToValueSetDto(INFORMANT_RECIPIENT_CODE, lookUpService.getSecurityRole())));
                return to;
            }).collect(toList());

            //Adding toActors to the fromActors.
            fromActors.addAll(toActors);

            consent.setActor(fromActors);
        }
        // set Medical Information
        setMedicalInformation(consentDto, consent);

        return consent;
    }

    private void setMedicalInformation(ConsentDto consentDto, Consent consent) {
        // Adding Medical Information
        Consent.ExceptComponent exceptComponent = new Consent.ExceptComponent();

        // List of included Sensitive policy codes
        exceptComponent.setType(Consent.ConsentExceptType.PERMIT);
        if (consentDto.isGeneralDesignation()) {
            // share all
            exceptComponent.setSecurityLabel(getIncludeCodingList(lookUpService.getSecurityLabel()));
        } else {
            // share the one user selects
            exceptComponent.setSecurityLabel(getIncludeCodingList(consentDto.getMedicalInformation()));
        }
        consent.setExcept(Collections.singletonList(exceptComponent));
    }

    private List<Coding> getIncludeCodingList(List<ValueSetDto> medicalInfoList) {
        // Set Exempt portion
        String systemUrl = configProperties.getConsent().getCodeSystem();
        // Get "share" categories from consent
        return medicalInfoList
                .stream()
                .map(valueSetDto -> new Coding(systemUrl, valueSetDto.getCode(), valueSetDto.getDisplay()))
                .collect(toList());
    }


    private Consent.ConsentActorComponent convertCareTeamToActor(CareTeam careTeam, ValueSetDto securityRoleValueSet) {
        Consent.ConsentActorComponent actor = new Consent.ConsentActorComponent();
        ReferenceDto referenceDto = new ReferenceDto();
        referenceDto.setReference("CareTeam/" + careTeam.getIdElement().getIdPart());
        referenceDto.setDisplay(careTeam.getName());
        actor.setReference(FhirDtoUtil.mapReferenceDtoToReference(referenceDto));
        actor.setRole(FhirDtoUtil.convertValuesetDtoToCodeableConcept(securityRoleValueSet));
        return actor;
    }

    private ReferenceDto convertCareTeamToReferenceDto(CareTeam careTeam) {
        ReferenceDto referenceDto = new ReferenceDto();
        referenceDto.setReference(careTeam.getIdElement().getIdPart());
        referenceDto.setDisplay(careTeam.getName());
        return referenceDto;
    }

    private boolean isDuplicate(ConsentDto consentDto, Optional<String> consentId) {
        //Duplicate Check For General Designation
        if (consentDto.isGeneralDesignation()) {
            Bundle consentBundle = fhirClient.search().forResource(Consent.class).where(new ReferenceClientParam("patient").hasId(consentDto.getPatient().getReference()))
                    .returnBundle(Bundle.class).execute();
            boolean checkFromBundle = consentBundle.getEntry().stream().anyMatch(consentBundleEntry -> {
                Consent consent = (Consent) consentBundleEntry.getResource();
                List<String> fromActor = getReferenceOfCareTeam(consent, INFORMANT_CODE);

                String pseudoOrgRef = getPseudoOrganization().getEntry().stream().findFirst().map(pseudoOrg -> {
                    Organization organization = (Organization) pseudoOrg.getResource();
                    return organization.getIdElement().getIdPart();
                }).get();
                if ((fromActor.size() == 1)) {
                    if (fromActor.stream().findFirst().get().equalsIgnoreCase("Organization/" + pseudoOrgRef)) {
                        return consentId.map(s -> !(s.equalsIgnoreCase(consent.getIdElement().getIdPart()))).orElse(true);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            });

            return checkFromBundle;
        }
        return false;
    }

    private List<String> getReferenceOfCareTeam(Consent consent, String code) {
        return consent.getActor().stream().filter(actor -> actor.getRole().getCoding().stream()
                .anyMatch(role -> role.getCode().equalsIgnoreCase(code)))
                .map(actor -> actor.getReference().getReference())
                .collect(toList());
    }

    private Bundle getPseudoOrganization() {
        return fhirClient.search().forResource(Organization.class)
                .where(new TokenClientParam("identifier").exactly().code(PSEUDO_ORGANIZATION_TAX_ID))
                .where(new StringClientParam("name").matches().value(PSEUDO_ORGANIZATION_NAME))
                .returnBundle(Bundle.class)
                .execute();
    }

    private List<String> getCareTeamIdsFromPractitioner(String practitioner) {
        IQuery careTeamQuery = fhirClient.search().forResource(CareTeam.class)
                .where(new ReferenceClientParam("participant").hasId(practitioner));

        Bundle careTeamBundle = (Bundle) careTeamQuery.returnBundle(Bundle.class).execute();

        return careTeamBundle.getEntry().stream().map(careTeamBundleEntry -> {
            CareTeam careTeam = (CareTeam) careTeamBundleEntry.getResource();
            return careTeam.getIdElement().getIdPart();
        }).collect(toList());

    }

    private DetailedConsentDto convertConsentDtoToDetailedConsentDto(ConsentDto consentDto) {

        List<AbstractCareTeamDto> fromOrganizationActors = consentDto.getFromActor().stream().filter(ac -> ac.getReference().contains("Organization"))
                .map(actor -> FhirUtil.getOrganizationActors(Optional.empty(), Optional.empty(), Optional.of(actor.getReference().replace("Organization/", "")), Optional.empty(), fhirClient, configProperties)
                        .stream().findAny().get()
                ).collect(toList());

        List<AbstractCareTeamDto> fromPractitionerActors = consentDto.getFromActor().stream().filter(ac -> ac.getReference().contains("Practitioner"))
                .map(actor -> FhirUtil.getPractitionerActors(Optional.empty(), Optional.empty(), Optional.of(actor.getReference().replace("Practitioner/", "")), Optional.empty(), fhirClient, configProperties)
                        .stream().findAny().get()
                ).collect(toList());

        List<AbstractCareTeamDto> fromRelatedPersons = consentDto.getFromActor().stream().filter(ac -> ac.getReference().contains("RelatedPerson"))
                .map(actor -> FhirUtil.getRelatedPersonActors(Optional.empty(), Optional.empty(), Optional.of(actor.getReference().replace("RelatedPerson/", "")), Optional.empty(), fhirClient, configProperties)
                        .stream().findAny().get()
                ).collect(toList());


        List<AbstractCareTeamDto> toOrganizationActors = consentDto.getToActor().stream().filter(ac -> ac.getReference().contains("Organization"))
                .map(actor -> FhirUtil.getOrganizationActors(Optional.empty(), Optional.empty(), Optional.of(actor.getReference().replace("Organization/", "")), Optional.empty(), fhirClient, configProperties)
                        .stream().findAny().get()
                ).collect(toList());

        List<AbstractCareTeamDto> toPractitionerActors = consentDto.getToActor().stream().filter(ac -> ac.getReference().contains("Practitioner"))
                .map(actor -> FhirUtil.getPractitionerActors(Optional.empty(), Optional.empty(), Optional.of(actor.getReference().replace("Practitioner/", "")), Optional.empty(), fhirClient, configProperties)
                        .stream().findAny().get()
                ).collect(toList());

        List<AbstractCareTeamDto> toRelatedPersons = consentDto.getToActor().stream().filter(ac -> ac.getReference().contains("RelatedPerson"))
                .map(actor -> FhirUtil.getRelatedPersonActors(Optional.empty(), Optional.empty(), Optional.of(actor.getReference().replace("RelatedPerson/", "")), Optional.empty(), fhirClient, configProperties)
                        .stream().findAny().get()
                ).collect(toList());


        List<ReferenceDto> toCareTeams = consentDto.getToActor().stream().filter(ac -> ac.getReference().contains("CareTeam")).collect(toList());

        DetailedConsentDto detailedConsentDto = new DetailedConsentDto();

        return detailedConsentDto.builder()
                .logicalId(consentDto.getLogicalId())
                .identifier(consentDto.getIdentifier())
                .category(consentDto.getCategory())
                .period(consentDto.getPeriod())
                .dateTime(consentDto.getDateTime())
                .status(consentDto.getStatus())
                .generalDesignation(consentDto.isGeneralDesignation())
                .patient(consentDto.getPatient())
                .fromOrganizationActors(fromOrganizationActors)
                .fromPractitionerActors(fromPractitionerActors)
                .fromRelatedPersons(fromRelatedPersons)
                .toOrganizationActors(toOrganizationActors)
                .toPractitionerActors(toPractitionerActors)
                .toRelatedPersons(toRelatedPersons)
                .toCareTeams(toCareTeams)
                .category(consentDto.getCategory())
                .purpose(consentDto.getPurpose())
                .medicalInformation(consentDto.getMedicalInformation())
                .sourceAttachment(consentDto.getSourceAttachment())
                .consentMedicalInfoType(consentDto.getConsentMedicalInfoType())
                .build();
    }


}
