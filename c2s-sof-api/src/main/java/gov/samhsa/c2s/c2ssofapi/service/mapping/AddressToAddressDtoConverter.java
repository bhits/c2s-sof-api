package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.AddressDto;
import org.hl7.fhir.dstu3.model.Address;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AddressToAddressDtoConverter extends AbstractConverter<Address, AddressDto> {

    @Override
    protected AddressDto convert(Address source) {
        AddressDto tempAddressDto = new AddressDto();
        if (source != null) {
            int numberOfLines = source.getLine().size();
            if (numberOfLines > 0) {
                tempAddressDto.setLine1(source.getLine().get(0).toString());

                if (numberOfLines > 1) {
                    tempAddressDto.setLine2(source.getLine().get(1).toString());
                }
            }

            tempAddressDto.setCity(source.getCity());
            if (source.getCountry() != null)
                tempAddressDto.setCountryCode(source.getCountry());
            if (source.getState() != null)
                tempAddressDto.setStateCode(source.getState());
            if (source.getUse() != null)
                tempAddressDto.setUse(source.getUse().toCode());
            tempAddressDto.setPostalCode(source.getPostalCode());
        }
        return tempAddressDto;
    }
}
