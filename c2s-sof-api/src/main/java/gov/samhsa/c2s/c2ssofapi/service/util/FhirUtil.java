package gov.samhsa.c2s.c2ssofapi.service.util;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.domain.KnownIdentifierSystemEnum;
import gov.samhsa.c2s.c2ssofapi.service.dto.AbstractCareTeamDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.AddressDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.IdentifierDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.FHIRClientException;
import gov.samhsa.c2s.c2ssofapi.service.exception.FHIRFormatErrorException;
import gov.samhsa.c2s.c2ssofapi.service.exception.IdentifierSystemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CareTeam;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.dstu3.model.codesystems.ContactPointSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ca.uhn.fhir.rest.api.Constants.PARAM_LASTUPDATED;

@Slf4j
public class FhirUtil {
    public static final int ACTIVITY_DEFINITION_FREQUENCY = 1;
    public static final int CARE_TEAM_END_DATE = 1;
    public static final int EPISODE_OF_CARE_END_PERIOD = 1;
    public static final String CARE_MANAGER_CODE = "CAREMNGR";
    public static final int PAGE_NUMBER = 2;

    public static final String OID_TEXT = "urn:oid:";
    public static final String URL_TEXT = "http";
    public static final String OID_NUMBER = "2.16";

    public static Enumerations.AdministrativeGender getPatientGender(String codeString) {
        switch (codeString.toUpperCase()) {
            case "MALE":
                return Enumerations.AdministrativeGender.MALE;
            case "M":
                return Enumerations.AdministrativeGender.MALE;
            case "FEMALE":
                return Enumerations.AdministrativeGender.FEMALE;
            case "F":
                return Enumerations.AdministrativeGender.FEMALE;
            case "OTHER":
                return Enumerations.AdministrativeGender.OTHER;
            case "O":
                return Enumerations.AdministrativeGender.OTHER;
            case "UNKNOWN":
                return Enumerations.AdministrativeGender.UNKNOWN;
            case "UN":
                return Enumerations.AdministrativeGender.UNKNOWN;
            default:
                return Enumerations.AdministrativeGender.UNKNOWN;
        }
    }

    public static Coding getCoding(String code, String display, String system) {
        Coding coding = new Coding();
        if (isStringNotNullAndNotEmpty(code)) {
            coding.setCode(code);
        }

        if (isStringNotNullAndNotEmpty(display)) {
            coding.setDisplay(display);
        }

        if (isStringNotNullAndNotEmpty(system)) {
            coding.setSystem(system);
        }
        return coding;
    }

    public static boolean checkPatientName(Patient patient, String searchValue) {
        return patient.getName()
                .stream()
                .anyMatch(humanName -> humanName.getGiven().stream().anyMatch(name -> name.toString().equalsIgnoreCase(searchValue)) || humanName.getFamily().equalsIgnoreCase(searchValue));
    }

    public static boolean checkPatientId(Patient patient, String searchValue) {
        return patient.getIdentifier()
                .stream()
                .anyMatch(identifier -> identifier.getValue().equalsIgnoreCase(searchValue));

    }

    public static boolean checkParticipantRole(List<CareTeam.CareTeamParticipantComponent> components, String role) {
        return components.stream()
                .filter(it -> it.getMember().getReference().contains(ResourceType.Practitioner.toString()))
                .map(it -> FhirUtil.getRoleFromCodeableConcept(it.getRole()))
                .anyMatch(t -> t.contains(role));
    }

    public static boolean isStringNotNullAndNotEmpty(String givenString) {
        return givenString != null && !givenString.trim().isEmpty();
    }

    public static boolean isStringNullOrEmpty(String givenString) {
        return givenString == null || givenString.trim().isEmpty();
    }


    public static IQuery setNoCacheControlDirective(IQuery searchQuery) {
        final CacheControlDirective cacheControlDirective = new CacheControlDirective();
        cacheControlDirective.setNoCache(true);
        searchQuery.cacheControl(cacheControlDirective);
        return searchQuery;
    }

    public static IQuery searchNoCache(IGenericClient fhirClient, Class resourceType, Optional<Boolean> sortByLastUpdatedTimeDesc){
        IQuery iQuery;
        if(sortByLastUpdatedTimeDesc.isPresent() && sortByLastUpdatedTimeDesc.get()){
            iQuery = fhirClient.search().forResource(resourceType).sort().descending(PARAM_LASTUPDATED);
        } else {
            iQuery = fhirClient.search().forResource(resourceType);
        }
        return setNoCacheControlDirective(iQuery);
    }

    public static IQuery setLastUpdatedTimeSortOrder(IQuery searchQuery, Boolean isDescending){
        if(isDescending){
            searchQuery.sort().descending(PARAM_LASTUPDATED);
        } else {
            searchQuery.sort().ascending(PARAM_LASTUPDATED);
        }
        return searchQuery;
    }


    public static String getRoleFromCodeableConcept(CodeableConcept codeableConcept) {
        Optional<Coding> codingRoleCode = codeableConcept.getCoding().stream().findFirst();
        return codingRoleCode.isPresent() ? codingRoleCode.get().getCode() : "";
    }

    public static Extension createExtension(String url, Type t) {
        Extension ext = new Extension();
        ext.setUrl(url);
        ext.setValue(t);
        return ext;
    }

    public static Optional<Coding> convertExtensionToCoding(Extension extension) {
        Optional<Coding> coding = Optional.empty();

        Type type = extension.getValue();
        if (type != null) {
            if (type instanceof CodeableConcept) {
                CodeableConcept codeableConcept = (CodeableConcept) type;

                List<Coding> codingList = codeableConcept.getCoding();

                if (codingList != null) {
                    coding = Optional.of(codingList.get(0));
                }
            }
        }

        return coding;
    }

    public static AddressDto convertAddressToAddressDto(Address address) {
        AddressDto addressDto = new AddressDto();
        if (address.hasLine()) {
            if (address.hasLine()) {
                address.getLine().stream().findAny().ifPresent(line -> addressDto.setLine1(line.getValue()));
            }
        }

        if (address.hasCity())
            addressDto.setCity(address.getCity());
        if (address.hasCountry())
            addressDto.setCountryCode(address.getCountry());
        if (address.hasPostalCode())
            addressDto.setPostalCode(address.getPostalCode());
        if (address.hasState())
            addressDto.setStateCode(address.getState());
        return addressDto;
    }

    public static IdentifierDto covertIdentifierToIdentifierDto(Identifier identifier) {
        IdentifierDto identifierDto = new IdentifierDto();
        identifierDto.setSystem(identifier.hasSystem() ? identifier.getSystem() : null);
        identifierDto.setValue(identifier.hasValue() ? identifier.getValue() : null);
        return identifierDto;
    }

    public static IdentifierDto entireCovertIdentifierToIdentifierDto(Identifier identifier) {
        IdentifierDto identifierDto = new IdentifierDto();

        if (identifier != null) {
            String systemOid = identifier.getSystem() != null ? identifier.getSystem() : "";
            String systemDisplay = null;

            try {
                if (systemOid.startsWith(OID_TEXT) || systemOid.startsWith(URL_TEXT)) {
                    systemDisplay = KnownIdentifierSystemEnum.fromUri(systemOid).getDisplay();
                } else if (systemOid.startsWith(OID_NUMBER)) {
                    systemDisplay = KnownIdentifierSystemEnum.fromOid(systemOid).getDisplay();
                } else
                    systemDisplay = systemOid;
            } catch (IdentifierSystemNotFoundException e) {
                systemDisplay = systemOid;
            }

            identifierDto = IdentifierDto.builder()
                    .system(systemOid)
                    .oid(systemOid.startsWith(OID_TEXT)
                            ? systemOid.replace(OID_TEXT, "")
                            : "")
                    .systemDisplay(systemDisplay)
                    .value(identifier.getValue())
                    .display(identifier.getValue())
                    .build();
        }
        return identifierDto;
    }

    public static List<Bundle.BundleEntryComponent> getAllBundleComponentsAsList(Bundle bundle, Optional<Integer> countSize, IGenericClient fhirClient, ConfigProperties configProperties) {
        int pageNumber = PAGE_NUMBER;
        int pageSize = countSize.orElse(configProperties.getFhir().getDefaultResourceBundlePageSize());
        Bundle updatedBundle = bundle;
        List<Bundle.BundleEntryComponent> bundleEntryComponents = new ArrayList<>();
        if (!bundle.getEntry().isEmpty()) {
            bundleEntryComponents.addAll(bundle.getEntry());

            while (updatedBundle.getLink(Bundle.LINK_NEXT) != null) {
                int offset = ((pageNumber >= 1 ? pageNumber : 1) - 1) * pageSize;
                String pageUrl = fhirClient.getServerBase()
                        + "?_getpages=" + bundle.getId()
                        + "&_getpagesoffset=" + offset
                        + "&_count=" + pageSize
                        + "&_bundletype=searchset";

                updatedBundle = fhirClient.search().byUrl(pageUrl).returnBundle(Bundle.class).execute();
                bundleEntryComponents.addAll(updatedBundle.getEntry());
                pageNumber++;
            }
        }
        return bundleEntryComponents;

    }

    public static void validateFhirResource(FhirValidator fhirValidator, DomainResource fhirResource,
                                            Optional<String> fhirResourceId, String fhirResourceName,
                                            String actionAndResourceName) {
        ValidationResult validationResult = fhirValidator.validateWithResult(fhirResource);

        if (fhirResourceId.isPresent()) {
            log.info(actionAndResourceName + " : " + "Validation successful? " + validationResult.isSuccessful() + " for " + fhirResourceName + " Id: " + fhirResourceId);
        } else {
            log.info(actionAndResourceName + " : " + "Validation successful? " + validationResult.isSuccessful());
        }

        if (!validationResult.isSuccessful()) {
            throw new FHIRFormatErrorException(fhirResourceName + " validation was not successful" + validationResult.getMessages());
        }
    }

    public static void createFhirResource(IGenericClient fhirClient, DomainResource fhirResource, String fhirResourceName) {
        try {
            MethodOutcome serverResponse = fhirClient.create().resource(fhirResource).execute();
            log.info("Created a new " + fhirResourceName + " : " + serverResponse.getId().getIdPart());
        } catch (BaseServerResponseException e) {
            log.error("Could NOT create " + fhirResourceName);
            throw new FHIRClientException("FHIR Client returned with an error while creating the " + fhirResourceName + " : " + e.getMessage());
        }
    }

    public static void updateFhirResource(IGenericClient fhirClient, DomainResource fhirResource, String actionAndResourceName) {
        try {
            MethodOutcome serverResponse = fhirClient.update().resource(fhirResource).execute();
            log.info(actionAndResourceName + " was successful for Id: " + serverResponse.getId().getIdPart());
        } catch (BaseServerResponseException e) {
            log.error("Could NOT " + actionAndResourceName + " with Id: " + fhirResource.getIdElement().getIdPart());
            throw new FHIRClientException("FHIR Client returned with an error during" + actionAndResourceName + " : " + e.getMessage());
        }
    }

    public static List<AbstractCareTeamDto> getOrganizationActors(Optional<String> patientId, Optional<String> name, Optional<String> organizationId, Optional<List<String>> careTeams, IGenericClient fhirClient, ConfigProperties configProperties){
        Bundle organizationBundle = fhirClient.search().forResource(Organization.class)
                .where(new TokenClientParam("_id").exactly().codes(participantIds(organizationId,patientId, careTeams, fhirClient)))
                .where(new RichStringClientParam("name").matches().value(name.orElse("")))
                .returnBundle(Bundle.class)
                .elementsSubset("id", "resourceType", "name", "identifier", "telecom", "address")
                .execute();
        List<Bundle.BundleEntryComponent> organizationBundleEntryList = getAllBundleComponentsAsList(organizationBundle, Optional.empty(), fhirClient, configProperties);

        return organizationBundleEntryList.stream().map(org -> {
            AbstractCareTeamDto abstractCareTeamDto = new AbstractCareTeamDto();
            Organization organization = (Organization) org.getResource();
            abstractCareTeamDto.setId(organization.getIdElement().getIdPart());
            abstractCareTeamDto.setDisplay(organization.getName());
            abstractCareTeamDto.setCareTeamType(AbstractCareTeamDto.CareTeamType.ORGANIZATION);
            List<IdentifierDto> identifierDtos = organization.getIdentifier().stream()
                    .map(identifier -> entireCovertIdentifierToIdentifierDto(identifier))
                    .collect(Collectors.toList());
            abstractCareTeamDto.setIdentifiers(identifierDtos);

            organization.getAddress().stream().findAny().ifPresent(address -> {
                AddressDto addressDto = convertAddressToAddressDto(address);
                abstractCareTeamDto.setAddress(addressDto);
            });

            if(organization.hasTelecom()) {
                organization.getTelecom().stream()
                        .filter(telecom -> {
                            if(telecom.hasSystem())
                                return telecom.getSystem().getDisplay().equalsIgnoreCase(ContactPointSystem.PHONE.toString());
                            return false;
                        })
                        .findAny().ifPresent(phone -> abstractCareTeamDto.setPhoneNumber(Optional.ofNullable(phone.getValue())));

                organization.getTelecom().stream().filter(telecom -> {
                    if(telecom.hasSystem())
                        return telecom.getSystem().getDisplay().equalsIgnoreCase(ContactPointSystem.EMAIL.toString());
                    return false;
                })
                        .findAny().ifPresent(email -> abstractCareTeamDto.setEmail(Optional.ofNullable(email.getValue())));
            }
            return abstractCareTeamDto;
        }).distinct().collect(Collectors.toList());
    }

    public static List<AbstractCareTeamDto> getPractitionerActors(Optional<String> patientId, Optional<String> name, Optional<String> practitionerId, Optional<List<String>> careTeams, IGenericClient fhirClient, ConfigProperties configProperties){
        Bundle practitionerBundle = fhirClient.search().forResource(Practitioner.class)
                .where(new TokenClientParam("_id").exactly().codes(participantIds(practitionerId,patientId, careTeams, fhirClient)))
                .where(new RichStringClientParam("name").matches().value(name.orElse("")))
                .returnBundle(Bundle.class)
                .elementsSubset("id", "resourceType", "name", "identifier", "telecom", "address")
                .execute();

        List<Bundle.BundleEntryComponent> practitionerBundleEntryList = FhirUtil.getAllBundleComponentsAsList(practitionerBundle, Optional.empty(), fhirClient, configProperties);

        return practitionerBundleEntryList.stream().map(pr -> {
            AbstractCareTeamDto abstractCareTeamDto = new AbstractCareTeamDto();
            Practitioner practitioner = (Practitioner) pr.getResource();
            abstractCareTeamDto.setId(practitioner.getIdElement().getIdPart());
            practitioner.getName().stream().findAny().ifPresent(humanName -> {
                abstractCareTeamDto.setDisplay(humanName.getGiven().stream().findAny().get() + " " + humanName.getFamily());
            });

            abstractCareTeamDto.setCareTeamType(AbstractCareTeamDto.CareTeamType.PRACTITIONER);
            List<IdentifierDto> identifierDtos = practitioner.getIdentifier().stream()
                    .map(identifier -> entireCovertIdentifierToIdentifierDto(identifier))
                    .collect(Collectors.toList());
            abstractCareTeamDto.setIdentifiers(identifierDtos);

            practitioner.getAddress().stream().findAny().ifPresent(address -> {
                        AddressDto addressDto = convertAddressToAddressDto(address);
                        abstractCareTeamDto.setAddress(addressDto);
                    }
            );

            if(practitioner.hasTelecom()) {
                practitioner.getTelecom().stream()
                        .filter(telecom -> {
                            if(telecom.hasSystem())
                                return telecom.getSystem().getDisplay().equalsIgnoreCase(ContactPointSystem.PHONE.toString());
                            return false;
                        })
                        .findAny().ifPresent(phone -> abstractCareTeamDto.setPhoneNumber(Optional.ofNullable(phone.getValue())));

                practitioner.getTelecom().stream().filter(telecom -> {
                    if(telecom.hasSystem())
                        return telecom.getSystem().getDisplay().equalsIgnoreCase(ContactPointSystem.EMAIL.toString());
                    return false;
                })
                        .findAny().ifPresent(email -> abstractCareTeamDto.setEmail(Optional.ofNullable(email.getValue())));
            }
            return abstractCareTeamDto;

        }).distinct().collect(Collectors.toList());
    }


    public static List<AbstractCareTeamDto> getRelatedPersonActors(Optional<String> patientId, Optional<String> name, Optional<String> relatedPersonId, Optional<List<String>> careTeams, IGenericClient fhirClient, ConfigProperties configProperties){

        Bundle relatedBundle = fhirClient.search().forResource(RelatedPerson.class)
                .where(new TokenClientParam("_id").exactly().codes(participantIds(relatedPersonId,patientId, careTeams, fhirClient)))
                .where(new RichStringClientParam("name").matches().value(name.orElse("")))
                .returnBundle(Bundle.class)
                .elementsSubset("id", "resourceType", "name", "identifier", "telecom", "address")
                .execute();

        List<Bundle.BundleEntryComponent> relatedPersonBundleEntryList = FhirUtil.getAllBundleComponentsAsList(relatedBundle, Optional.empty(), fhirClient, configProperties);

        return  relatedPersonBundleEntryList.stream().map(rp -> {
            AbstractCareTeamDto abstractCareTeamDto = new AbstractCareTeamDto();
            RelatedPerson relatedPerson = (RelatedPerson) rp.getResource();
            abstractCareTeamDto.setId(relatedPerson.getIdElement().getIdPart());

            abstractCareTeamDto.setCareTeamType(AbstractCareTeamDto.CareTeamType.RELATEDPERSON);
            relatedPerson.getName().stream().findAny().ifPresent(humanName -> {
                abstractCareTeamDto.setDisplay(humanName.getGiven().stream().findAny().get() + " " + humanName.getFamily());
            });

            List<IdentifierDto> identifierDtos = relatedPerson.getIdentifier().stream()
                    .map(identifier -> entireCovertIdentifierToIdentifierDto(identifier))
                    .collect(Collectors.toList());
            abstractCareTeamDto.setIdentifiers(identifierDtos);

            relatedPerson.getAddress().stream().findAny().ifPresent(address -> {
                        AddressDto addressDto = convertAddressToAddressDto(address);
                        abstractCareTeamDto.setAddress(addressDto);
                    }
            );

            if(relatedPerson.hasTelecom()) {
                relatedPerson.getTelecom().stream()
                        .filter(telecom -> {
                            if(telecom.hasSystem())
                                return telecom.getSystem().getDisplay().equalsIgnoreCase(ContactPointSystem.PHONE.toString());
                            return false;
                        })
                        .findAny().ifPresent(phone -> abstractCareTeamDto.setPhoneNumber(Optional.ofNullable(phone.getValue())));

                relatedPerson.getTelecom().stream().filter(telecom -> {
                    if(telecom.hasSystem())
                        return telecom.getSystem().getDisplay().equalsIgnoreCase(ContactPointSystem.EMAIL.toString());
                    return false;
                })
                        .findAny().ifPresent(email -> abstractCareTeamDto.setEmail(Optional.ofNullable(email.getValue())));
            }
            return abstractCareTeamDto;
        }).distinct().collect(Collectors.toList());
    }

    public static List<String> getCareTeamParticipantIdsFromPatient(Optional<String> patientId,IGenericClient fhirClient) {
        List<String> participantIds = new ArrayList<>();
        if (patientId.isPresent()) {
            Bundle careTeamBundle = fhirClient.search().forResource(CareTeam.class).where(new ReferenceClientParam("patient").hasId(patientId.get()))
                    .returnBundle(Bundle.class)
                    .elementsSubset("participant")
                    .execute();
            participantIds = careTeamBundle.getEntry().stream().flatMap(careTeam -> {
                CareTeam ct = (CareTeam) careTeam.getResource();
                return ct.getParticipant().stream().map(par -> {
                    String references = par.getMember().getReference().split("/")[1];
                    return references;
                });
            }).collect(Collectors.toList());
        }
        return participantIds;
    }


    public static List<String> participantIds(Optional<String> participantId,Optional<String> patientId, Optional<List<String>> careTeams, IGenericClient fhirClient){
        List<String> participantIds=new ArrayList<>();
        if(participantId.isPresent()){
            participantIds= Arrays.asList(participantId.get());
        }else if(careTeams.isPresent()) {
            participantIds=getParticipantIdFromCareTeam(careTeams,fhirClient);
        }else{
            participantIds=getCareTeamParticipantIdsFromPatient(patientId,fhirClient);
        }
        return participantIds;
    }

    public static List<String> getParticipantIdFromCareTeam(Optional<List<String>> careTeams,IGenericClient fhirClient){
        Bundle bundle= fhirClient.search().forResource(CareTeam.class).where(new TokenClientParam("_id").exactly().codes(careTeams.get())).returnBundle(Bundle.class).execute();

        return bundle.getEntry().stream().map(ct-> (CareTeam) ct.getResource()).flatMap(ct->ct.getParticipant().stream().map(par->par.getMember().getReference().split("/")[1])).distinct().collect(Collectors.toList());

    }


}

