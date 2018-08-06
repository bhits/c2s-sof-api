package gov.samhsa.c2s.c2ssofapi.service.util;

import gov.samhsa.c2s.c2ssofapi.service.dto.ValueSetDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.ValueSet;

@Slf4j
public class LookUpUtil {

    public static boolean isValueSetResponseValid(ValueSet response, String type) {
        boolean isValid = true;
        if (response == null ||
                response.getExpansion() == null ||
                response.getExpansion().getContains() == null ||
                response.getExpansion().getContains().isEmpty()) {
            isValid = false;
        }
        return isValid;
    }

    public static boolean isValueSetAvailableInServer(ValueSet response, String type) {
        return isValidResponseOrThrowException(response, type, true);
    }

    public static boolean isValidResponseOrThrowException(ValueSet response, String type, boolean throwException) {
        boolean isValid = isValueSetResponseValid(response, type);
        if (!isValid && throwException) {
            log.error("Query was successful, but found no " + type + " codes in the configured FHIR server");
            throw new ResourceNotFoundException("Query was successful, but found no " + type + " codes in the configured FHIR server");
        }
        return isValid;
    }

    public static ValueSetDto convertConceptReferenceToValueSetDto(ValueSet.ConceptReferenceComponent conceptReferenceComponent, String codingSystemUrl) {
        ValueSetDto valueSetDto = new ValueSetDto();
        valueSetDto.setCode(conceptReferenceComponent.getCode());
        valueSetDto.setDisplay(conceptReferenceComponent.getDisplay());
        valueSetDto.setSystem(codingSystemUrl);
        return valueSetDto;
    }

    public static ValueSetDto convertExpansionComponentToValueSetDto(ValueSet.ValueSetExpansionContainsComponent expansionComponent) {
        ValueSetDto valueSetDto = new ValueSetDto();
        valueSetDto.setSystem(expansionComponent.getSystem());
        valueSetDto.setCode(expansionComponent.getCode());
        valueSetDto.setDisplay(expansionComponent.getDisplay());
        return valueSetDto;
    }
}
