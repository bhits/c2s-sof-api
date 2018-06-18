package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.TelecomDto;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TelecomListToTelecomDtoListConverter extends AbstractConverter<List<ContactPoint>, List<TelecomDto>> {

    @Override
    protected List<TelecomDto> convert(List<ContactPoint> source) {
        List<TelecomDto> telecomDtoList = new ArrayList<>();

        if (source != null && source.size() > 0) {

            for (ContactPoint tempTelecom : source) {
                TelecomDto tempTelecomDto = new TelecomDto();
                tempTelecomDto.setValue(Optional.ofNullable(tempTelecom.getValue()));
                if (tempTelecom.getSystem() != null)
                    tempTelecomDto.setSystem(Optional.ofNullable(tempTelecom.getSystem().toCode()));
                if (tempTelecom.getUse() != null)
                    tempTelecomDto.setUse(Optional.ofNullable(tempTelecom.getUse().toCode()));
                telecomDtoList.add(tempTelecomDto);
            }
        }
        return telecomDtoList;
    }
}
