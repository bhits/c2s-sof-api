package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.PeriodDto;
import gov.samhsa.c2s.c2ssofapi.service.util.DateUtil;
import org.hl7.fhir.dstu3.model.Period;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class PeriodToPeriodDtoConverter extends AbstractConverter<Period,PeriodDto> {
    @Override
    protected PeriodDto convert(Period period) {

        PeriodDto periodDto = new PeriodDto();
        periodDto.setStart((period.hasStart()) ? DateUtil.convertDateToLocalDate(period.getStart()) : null);
        periodDto.setEnd((period.hasEnd()) ? DateUtil.convertDateToLocalDate(period.getEnd()) : null);

        return periodDto;
    }
}
