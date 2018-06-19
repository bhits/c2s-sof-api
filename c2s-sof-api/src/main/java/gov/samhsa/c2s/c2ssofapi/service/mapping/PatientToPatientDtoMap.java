package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;
import org.hl7.fhir.dstu3.model.Patient;
import org.modelmapper.Converter;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Component
public class PatientToPatientDtoMap extends PropertyMap<Patient, PatientDto> {

    private final AddressListToAddressDtoListConverter addressListToAddressDtoListConverter;
    private final TelecomListToTelecomDtoListConverter telecomListToTelecomDtoListConverter;
    private final IdentifierListToIdentifierDtoListConverter identifierListToIdentifierDtoListConverter;
    private final HumanNameListToNameDtoListConverter humanNameListToNameDtoListConverter;

    Converter<Date, LocalDate> DateToLocalDate =
            ctx -> ctx.getSource() == null ? null : ctx.getSource().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    public PatientToPatientDtoMap(AddressListToAddressDtoListConverter addressListToAddressDtoListConverter, TelecomListToTelecomDtoListConverter telecomListToTelecomDtoListConverter, IdentifierListToIdentifierDtoListConverter
            identifierListToIdentifierDtoListConverter, HumanNameListToNameDtoListConverter humanNameListToNameDtoListConverter) {
        this.addressListToAddressDtoListConverter = addressListToAddressDtoListConverter;
        this.telecomListToTelecomDtoListConverter = telecomListToTelecomDtoListConverter;
        this.identifierListToIdentifierDtoListConverter = identifierListToIdentifierDtoListConverter;
        this.humanNameListToNameDtoListConverter = humanNameListToNameDtoListConverter;
    }

    @Override
    protected void configure() {
        map().setResourceURL(source.getId());
        map().setActive(source.getActive());

        using(addressListToAddressDtoListConverter).map(source.getAddress()).setAddresses(null);
        using(telecomListToTelecomDtoListConverter).map(source.getTelecom()).setTelecoms(null);
        using(identifierListToIdentifierDtoListConverter).map(source.getIdentifier()).setIdentifier(null);
        using(DateToLocalDate).map(source.getBirthDate()).setBirthDate(null);
        using(humanNameListToNameDtoListConverter).map(source.getName()).setName(null);
    }


}
