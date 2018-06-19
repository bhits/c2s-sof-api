package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.ValueSetDto;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class CodeableConceptListToValueSetDtoListConverter extends AbstractConverter<List<CodeableConcept>, List<ValueSetDto>> {


    @Override
    protected List<ValueSetDto> convert(List<CodeableConcept> source) {
        ValueSetDto valueSetDto = new ValueSetDto();
        List<ValueSetDto> valueSetDtos = new ArrayList<>();

        if (source != null && source.size() > 0) {
            int numberOfCategories = source.get(0).getCoding().size();
            if (numberOfCategories > 0) {
                source.get(0).getCoding().forEach(coding -> {
                    valueSetDto.setSystem(coding.getSystem());
                    valueSetDto.setDisplay(coding.getDisplay());
                    valueSetDto.setCode(coding.getCode());
                    valueSetDtos.add(valueSetDto);
                });
            }
        }
        return valueSetDtos;

    }
}
