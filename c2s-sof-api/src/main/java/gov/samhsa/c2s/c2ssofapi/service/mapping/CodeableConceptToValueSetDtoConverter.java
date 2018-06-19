package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.ValueSetDto;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;


@Component
public class CodeableConceptToValueSetDtoConverter extends AbstractConverter<CodeableConcept,ValueSetDto> {
    @Override
    protected ValueSetDto convert(CodeableConcept  source) {
        ValueSetDto tempValueSetDto=new ValueSetDto();
        if(source !=null){
            if(source.getCodingFirstRep().getDisplay() !=null)
                tempValueSetDto.setDisplay(source.getCodingFirstRep().getDisplay());
            if(source.getCodingFirstRep().getSystem()!=null)
                tempValueSetDto.setSystem(source.getCodingFirstRep().getSystem());
            if(source.getCodingFirstRep().getCode()!=null)
                tempValueSetDto.setCode(source.getCodingFirstRep().getCode());
        }
        return tempValueSetDto;
    }

}
