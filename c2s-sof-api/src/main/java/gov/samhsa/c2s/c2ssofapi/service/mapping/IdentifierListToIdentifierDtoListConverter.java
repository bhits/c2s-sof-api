package gov.samhsa.c2s.c2ssofapi.service.mapping;

import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
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
    private final String OID_TEXT = "urn:oid:";
    private final String URL_TEXT = "http";
    private final String OID_NUMBER = "2.16";
    private final ConfigProperties fisProperties;

    public IdentifierListToIdentifierDtoListConverter(ConfigProperties fisProperties) {
        this.fisProperties = fisProperties;
    }

    @Override
    protected List<IdentifierDto> convert(List<Identifier> source) {
        List<IdentifierDto> identifierDtos = new ArrayList<>();
        if (source != null && source.size() > 0) {
            for (Identifier identifier : source) {
                String systemOid = identifier.getSystem() != null ? identifier.getSystem() : "";
                if(systemOid.startsWith(OID_TEXT)){
                    //OID does not contain "urn...". If set improperly, remove it
                    String[] arrOfStr = systemOid.split(OID_TEXT);
                    systemOid = arrOfStr[1];
                }
                String systemDisplay;

                try {
                    if (systemOid.equalsIgnoreCase(fisProperties.getPatient().getMrn().getCodeSystem()) || systemOid.equalsIgnoreCase(fisProperties.getPatient().getMrn().getCodeSystemOID())) {
                        systemDisplay = fisProperties.getPatient().getMrn().getDisplayName();
                    } else if (systemOid.startsWith(OID_TEXT) || systemOid.startsWith(URL_TEXT)) {
                        systemDisplay = KnownIdentifierSystemEnum.fromUri(systemOid).getDisplay();
                    } else if (systemOid.startsWith(OID_NUMBER)) {
                        systemDisplay = KnownIdentifierSystemEnum.fromOid(systemOid).getDisplay();
                    } else
                        systemDisplay = systemOid;
                } catch (IdentifierSystemNotFoundException e) {
                    systemDisplay = systemOid;
                }

                identifierDtos.add(
                        IdentifierDto.builder()
                                .system(systemOid)
                                .oid(systemOid.startsWith(OID_TEXT)
                                        ? systemOid.replace(OID_TEXT, "")
                                        : "")
                                .systemDisplay(systemDisplay)
                                .value(identifier.getValue())
                                .display(identifier.getValue())
                                .build()
                );
            }
        }
        return identifierDtos;
    }
}
