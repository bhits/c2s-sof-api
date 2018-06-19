package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.AddressDto;
import org.hl7.fhir.dstu3.model.Address;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddressListToAddressDtoListConverter extends AbstractConverter<List<Address>, List<AddressDto>> {

    @Override
    protected List<AddressDto> convert(List<Address> source) {
        List<AddressDto> addressDtos = new ArrayList<>();

        if (source != null && source.size() > 0) {

            for (Address address : source) {

                addressDtos.add(                AddressDto.builder().line1(
                        address.getLine().size() > 0 ?
                                address.getLine().get(0).toString()
                                : "")
                        .line2(address.getLine().size() > 1 ?
                                address.getLine().get(1).toString()
                                : "")
                        .city(address.getCity())
                        .stateCode(address.getState())
                        .countryCode(address.getCountry())
                        .postalCode(address.getPostalCode())
                        .build());
            }
        }
        return addressDtos;
    }
}
