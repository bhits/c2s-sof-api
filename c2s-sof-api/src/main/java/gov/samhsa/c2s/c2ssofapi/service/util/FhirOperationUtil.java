package gov.samhsa.c2s.c2ssofapi.service.util;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.domain.StructureDefinitionEnum;
import gov.samhsa.c2s.c2ssofapi.service.exception.FHIRClientException;
import gov.samhsa.c2s.c2ssofapi.service.exception.FHIRFormatErrorException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.UriType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ca.uhn.fhir.rest.api.Constants.PARAM_LASTUPDATED;

@Slf4j
public class FhirOperationUtil {

    private static final int PAGE_NUMBER = 2;

    public static void validateFhirResource(FhirValidator fhirValidator, DomainResource fhirResource,
                                            Optional<String> fhirResourceId, String fhirResourceName,
                                            String actionAndResourceName) {
        ValidationResult validationResult = fhirValidator.validateWithResult(fhirResource);

        if (fhirResourceId.isPresent()) {
            log.info(actionAndResourceName + " : " + "Validation successful? " + validationResult.isSuccessful() + " for " + fhirResourceName + " Id: " + fhirResourceId);
        } else {
            log.info(actionAndResourceName + " : " + "Validation successful? " + validationResult.isSuccessful());
        }

        if (!validationResult.isSuccessful()) {
            log.info("Listing the issues found when validating the " + fhirResourceName + "(" + actionAndResourceName + ") :");
            fhirResourceId.ifPresent(s -> log.info("FHIR Resource ID: " + s));
            // Show the issues
            for (SingleValidationMessage next : validationResult.getMessages()) {
                log.error("Next issue (" + next.getSeverity() + ") - " + next.getLocationString() + " - " + next.getMessage());
            }
            throw new FHIRFormatErrorException(fhirResourceName + " validation was not successful" + validationResult.getMessages());
        }
    }

    public static void createFhirResource(IGenericClient fhirClient, DomainResource fhirResource, String fhirResourceName) {
        try {
            MethodOutcome serverResponse = fhirClient.create().resource(fhirResource).execute();
            log.info("Created a new " + fhirResourceName + " : " + serverResponse.getId().getIdPart());
        } catch (BaseServerResponseException e) {
            log.error("Could NOT create " + fhirResourceName);
            throw new FHIRClientException("FHIR Client returned with an error while creating the " + fhirResourceName + " : " + e.getMessage());
        }
    }

    public static void updateFhirResource(IGenericClient fhirClient, DomainResource fhirResource, String actionAndResourceName) {
        try {
            MethodOutcome serverResponse = fhirClient.update().resource(fhirResource).execute();
            log.info(actionAndResourceName + " was successful for Id: " + serverResponse.getId().getIdPart());
        } catch (BaseServerResponseException e) {
            log.error("Could NOT " + actionAndResourceName + " with Id: " + fhirResource.getIdElement().getIdPart());
            throw new FHIRClientException("FHIR Client returned with an error during" + actionAndResourceName + " : " + e.getMessage());
        }
    }

    public static IQuery setNoCacheControlDirective(IQuery searchQuery) {
        final CacheControlDirective cacheControlDirective = new CacheControlDirective();
        cacheControlDirective.setNoCache(true);
        searchQuery.cacheControl(cacheControlDirective);
        return searchQuery;
    }

    public static IQuery searchNoCache(IGenericClient fhirClient, Class resourceType, Optional<Boolean> sortByLastUpdatedTimeDesc) {
        IQuery iQuery;
        if (sortByLastUpdatedTimeDesc.isPresent() && sortByLastUpdatedTimeDesc.get()) {
            iQuery = fhirClient.search().forResource(resourceType).sort().descending(PARAM_LASTUPDATED);
        } else {
            iQuery = fhirClient.search().forResource(resourceType);
        }
        return setNoCacheControlDirective(iQuery);
    }

    public static IQuery setLastUpdatedTimeSortOrder(IQuery searchQuery, Boolean isDescending) {
        if (isDescending) {
            searchQuery.sort().descending(PARAM_LASTUPDATED);
        } else {
            searchQuery.sort().ascending(PARAM_LASTUPDATED);
        }
        return searchQuery;
    }

    public static List<Bundle.BundleEntryComponent> getAllBundleComponentsAsList(Bundle bundle, Optional<Integer> countSize, IGenericClient fhirClient, ConfigProperties configProperties) {
        int pageNumber = PAGE_NUMBER;
        int pageSize = countSize.orElse(configProperties.getFhir().getDefaultResourceBundlePageSize());
        Bundle updatedBundle = bundle;
        List<Bundle.BundleEntryComponent> bundleEntryComponents = new ArrayList<>();
        if (!bundle.getEntry().isEmpty()) {
            bundleEntryComponents.addAll(bundle.getEntry());

            while (updatedBundle.getLink(Bundle.LINK_NEXT) != null) {
                int offset = ((pageNumber >= 1 ? pageNumber : 1) - 1) * pageSize;
                String pageUrl = fhirClient.getServerBase()
                        + "?_getpages=" + bundle.getId()
                        + "&_getpagesoffset=" + offset
                        + "&_count=" + pageSize
                        + "&_bundletype=searchset";

                updatedBundle = fhirClient.search().byUrl(pageUrl).returnBundle(Bundle.class).execute();
                bundleEntryComponents.addAll(updatedBundle.getEntry());
                pageNumber++;
            }
        }
        return bundleEntryComponents;
    }

    public static List<UriType> getURIList(IGenericClient fhirClient, String resource) {
        Bundle structureDefinitionBundle = null;

        switch (resource.toUpperCase()) {
            case "CONSENT":
                structureDefinitionBundle = fhirClient.search().forResource(StructureDefinition.class)
                        .where(new TokenClientParam("type").exactly().code("Consent"))
                        .returnBundle(Bundle.class)
                        .execute();
                break;
             default:

        }

        if (structureDefinitionBundle != null && !structureDefinitionBundle.getEntry().isEmpty()) {
            //First check in server
            log.info("Number of Structure Definitions found:" + structureDefinitionBundle.getTotal() + " for " + resource);

            List<StructureDefinition> structureDefinitionList = structureDefinitionBundle.getEntry().stream()
                    .filter(bundle -> bundle.getResource().getResourceType().equals(ResourceType.StructureDefinition))
                    .map(structureDefinition -> (StructureDefinition) structureDefinition.getResource())
                    .collect(Collectors.toList());
            return structureDefinitionList.stream().map(StructureDefinition::getUrlElement).collect(Collectors.toList());
        } else {
            //Return URI List from ENUM
            log.info("No StructureDefinition found...Getting URL from ENUM for " + resource);
            try {
                String url = StructureDefinitionEnum.valueOf(resource.toUpperCase()).getUrl();
                if (url != null && !url.isEmpty()) {
                    return Collections.singletonList(new UriType(url));
                }
            } catch (Exception e) {
                log.error("Neither StructureDefinition nor ENUM constant found");
                // Don't get stuck here
                return null;
            }
        }
        return null;
    }
}

