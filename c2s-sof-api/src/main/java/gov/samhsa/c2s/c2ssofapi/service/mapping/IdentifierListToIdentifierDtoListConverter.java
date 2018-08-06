package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.constants.IdentifierConstants;
import gov.samhsa.c2s.c2ssofapi.domain.KnownIdentifierSystemEnum;
import gov.samhsa.c2s.c2ssofapi.service.dto.IdentifierDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.IdentifierSystemNotFoundException;
import org.hl7.fhir.dstu3.model.Identifier;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IdentifierListToIdentifierDtoListConverter extends AbstractConverter<List<Identifier>, List<IdentifierDto>> {

    private final ConfigProperties fisProperties;

    public IdentifierListToIdentifierDtoListConverter(ConfigProperties fisProperties) {
        this.fisProperties = fisProperties;
    }

    @Override
    protected List<IdentifierDto> convert(List<Identifier> source) {
        List<IdentifierDto> identifierDtos = new ArrayList<>();
        if (source != null && source.size() > 0) {
            for (Identifier identifier : source) {
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
                        // It's an URI
                        systemDisplay = KnownIdentifierSystemEnum.fromUri(idSystem).getDisplay();
                        oid = KnownIdentifierSystemEnum.fromUri(idSystem).getOid();
                    } else if (idSystem.startsWith(IdentifierConstants.OID_NUMBER_STARTING_WITH)) {
                        // It's an OID
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

                identifierDtos.add(
                        IdentifierDto.builder()
                                .system(idSystem)
                                .oid(oid)
                                .systemDisplay(systemDisplay)
                                .value(identifier.getValue())
                                .display(systemDisplay)
                                .build()
                );
            }
        }
        return identifierDtos;
    }
}
