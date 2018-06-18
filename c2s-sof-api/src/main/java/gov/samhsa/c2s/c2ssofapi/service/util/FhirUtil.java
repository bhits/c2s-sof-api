package gov.samhsa.c2s.c2ssofapi.service.util;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import gov.samhsa.c2s.c2ssofapi.domain.KnownIdentifierSystemEnum;
import gov.samhsa.c2s.c2ssofapi.service.dto.AddressDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.IdentifierDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.IdentifierSystemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CareTeam;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.Type;

import java.util.List;
import java.util.Optional;

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


}

