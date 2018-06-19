package gov.samhsa.c2s.c2ssofapi.service.mapping;

import org.hl7.fhir.dstu3.model.Resource;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component

public class ResourceIdToLogicalIdConverter extends AbstractConverter<Resource,String> {

    @Override
    protected String convert(Resource source){
        String logicalId ="";

        if (source.hasIdElement() && source.getIdElement().hasIdPart()) {
            logicalId = source.getIdElement().getIdPart();
        }
        return logicalId;
    }

}
