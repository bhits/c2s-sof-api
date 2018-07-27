package gov.samhsa.c2s.c2ssofapi.service.util;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.ActivityDefinition;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.UriType;

import java.util.List;

@Slf4j
public class FhirProfileUtil {

    public static void setConsentProfileMetaData(IGenericClient fhirClient, Consent consent) {
        List<UriType> uriList = FhirOperationUtil.getURIList(fhirClient, ResourceType.Consent.toString());
        if (uriList != null && !uriList.isEmpty()) {
            Meta meta = new Meta().setProfile(uriList);
            consent.setMeta(meta);
        }
    }

    public static void setOrganizationProfileMetaData(IGenericClient fhirClient, Organization organization) {
        List<UriType> uriList = FhirOperationUtil.getURIList(fhirClient, ResourceType.Organization.toString());
        if (uriList != null && !uriList.isEmpty()) {
            Meta meta = new Meta().setProfile(uriList);
            organization.setMeta(meta);
        }
    }

    public static void setActivityDefinitionProfileMetaData(IGenericClient fhirClient, ActivityDefinition activityDefinition) {
        List<UriType> uriList = FhirOperationUtil.getURIList(fhirClient, ResourceType.ActivityDefinition.toString());
        if (uriList != null && !uriList.isEmpty()) {
            Meta meta = new Meta().setProfile(uriList);
            activityDefinition.setMeta(meta);
        }
    }
}
