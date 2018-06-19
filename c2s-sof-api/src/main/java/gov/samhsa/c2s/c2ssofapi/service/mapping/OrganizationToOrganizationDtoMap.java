package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.OrganizationDto;
import org.hl7.fhir.dstu3.model.Organization;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrganizationToOrganizationDtoMap extends PropertyMap<Organization, OrganizationDto> {

    @Autowired
    private IdentifierListToIdentifierDtoListConverter identifierListToIdentifierDtoListConverter;

    @Autowired
    private AddressListToAddressDtoListConverter addressListToAddressDtoListConverter;

    @Autowired
    private TelecomListToTelecomDtoListConverter telecomListToTelecomDtoListConverter;

    @Override
    protected void configure() {
        map().setName(source.getName());
        using(identifierListToIdentifierDtoListConverter).map(source.getIdentifier()).setIdentifiers(null);
        using(addressListToAddressDtoListConverter).map(source.getAddress()).setAddresses(null);
        using(telecomListToTelecomDtoListConverter).map(source.getTelecom()).setTelecoms(null);
    }
}
