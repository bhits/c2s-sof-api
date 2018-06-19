package gov.samhsa.c2s.c2ssofapi.service.mapping;

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

    @Override
    protected List<IdentifierDto> convert(List<Identifier> source) {
        List<IdentifierDto> identifierDtos = new ArrayList<>();
        if (source != null && source.size() > 0) {
            for (Identifier identifier : source) {
                String systemOid = identifier.getSystem() != null ? identifier.getSystem() : "";

                String systemDisplay = null;

                try {
                    if (systemOid.startsWith(OID_TEXT) || systemOid.startsWith("http")) {
                        systemDisplay = KnownIdentifierSystemEnum.fromUri(systemOid).getDisplay();
                    } else if (systemOid.startsWith("2.16")) {
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
