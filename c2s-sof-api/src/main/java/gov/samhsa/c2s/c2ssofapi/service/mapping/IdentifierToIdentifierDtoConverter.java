package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.constants.IdentifierConstants;
import gov.samhsa.c2s.c2ssofapi.domain.KnownIdentifierSystemEnum;
import gov.samhsa.c2s.c2ssofapi.service.dto.IdentifierDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.IdentifierSystemNotFoundException;
import org.hl7.fhir.dstu3.model.Identifier;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class IdentifierToIdentifierDtoConverter extends AbstractConverter<Identifier, IdentifierDto> {

    private final ConfigProperties fisProperties;

    IdentifierDto identifierDto;

    public IdentifierToIdentifierDtoConverter(ConfigProperties fisProperties) {
        this.fisProperties = fisProperties;
    }

    @Override
    protected IdentifierDto convert(Identifier identifier) {

        if (identifier != null) {
            String idSystem = identifier.getSystem() != null ? identifier.getSystem() : "";
            String idSystemWithNoUrn = "";
            String oid = "";
            if(idSystem.startsWith(IdentifierConstants.URN_OID_TEXT)){
                String[] arrOfStr = idSystem.split(IdentifierConstants.URN_OID_TEXT);
                idSystemWithNoUrn = arrOfStr[1];
            }
            String systemDisplay;

            try {
                if (idSystemWithNoUrn.equalsIgnoreCase(fisProperties.getPatient().getMrn().getCodeSystemOID()) || idSystem.equalsIgnoreCase(fisProperties.getPatient().getMrn().getCodeSystem())) {
                    // System Default
                    systemDisplay = fisProperties.getPatient().getMrn().getDisplayName();
                    oid = fisProperties.getPatient().getMrn().getCodeSystemOID();
                } else if (idSystem.startsWith(IdentifierConstants.URN_OID_TEXT) || idSystem.startsWith(IdentifierConstants.HTTP_TEXT)) {
                    systemDisplay = KnownIdentifierSystemEnum.fromUri(idSystem).getDisplay();
                    oid = KnownIdentifierSystemEnum.fromUri(idSystem).getOid();
                } else if (idSystem.startsWith(IdentifierConstants.OID_NUMBER_STARTING_WITH)) {
                    systemDisplay = KnownIdentifierSystemEnum.fromOid(idSystem).getDisplay();
                    oid = idSystem;
                } else
                    systemDisplay = idSystem;
            } catch (IdentifierSystemNotFoundException e) {
                systemDisplay = idSystem;
            }

            if(oid == null || oid.isEmpty()){
                oid = idSystem.startsWith(IdentifierConstants.URN_OID_TEXT) ? idSystem.replace(IdentifierConstants.URN_OID_TEXT, ""): "";
            }

            identifierDto = IdentifierDto.builder()
                    .system(idSystem)
                    .oid(oid)
                    .systemDisplay(systemDisplay)
                    .value(identifier.getValue())
                    .display(identifier.getValue())
                    .build();
        }
        return identifierDto;
    }

}

