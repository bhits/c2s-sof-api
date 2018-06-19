package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.ValueSetDto;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Consent;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExceptComponentToValueSetDtoConverter extends AbstractConverter<List<Consent.ExceptComponent>, List<ValueSetDto>> {

    @Override
    protected List<ValueSetDto> convert(List<Consent.ExceptComponent> source) {
        List<ValueSetDto> valueSetDtos = new ArrayList<>();

        if (source != null && source.size() > 0 && source.get(0) != null) {
            List<Coding> codings = source.get(0).getSecurityLabel();
            for (Coding coding : codings) {
                ValueSetDto valueSetDto = new ValueSetDto();
                valueSetDto.setCode(coding.getCode());
                valueSetDto.setDisplay(coding.getDisplay());
                valueSetDto.setSystem(coding.getSystem());
                valueSetDtos.add(valueSetDto);
            }
        }
        return valueSetDtos;
    }
}
