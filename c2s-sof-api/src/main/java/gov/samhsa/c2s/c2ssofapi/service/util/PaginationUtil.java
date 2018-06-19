package gov.samhsa.c2s.c2ssofapi.service.util;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ca.uhn.fhir.rest.api.Constants.PARAM_COUNT;
import static ca.uhn.fhir.rest.api.Constants.PARAM_PAGINGACTION;
import static ca.uhn.fhir.rest.api.Constants.PARAM_PAGINGOFFSET;

@Slf4j
public final class PaginationUtil {

    public static Bundle getSearchBundleFirstPage(IQuery query, int count, Optional<Include> include) {
        if (include.isPresent()) {
            return (Bundle) query.count(count)
                    .include(include.get())
                    .returnBundle(Bundle.class)
                    .encodedJson()
                    .execute();
        } else {
            return (Bundle) query.count(count)
                    .returnBundle(Bundle.class)
                    .encodedJson()
                    .execute();
        }
    }

    public static Bundle getSearchBundleAfterFirstPage(IGenericClient fhirClient, ConfigProperties configProperties, Bundle SearchBundle, int pageNumber, int pageSize) {
        if (SearchBundle.getLink(Bundle.LINK_NEXT) != null) {
            //Assuming page number starts with 1
            int offset = ((pageNumber >= 1 ? pageNumber : 1) - 1) * pageSize;

            if (offset >= SearchBundle.getTotal()) {
                throw new ResourceNotFoundException("No resources were found in the FHIR server for the page number: " + pageNumber);
            }

            String pageUrl = configProperties.getFhir().getServerUrl()
                    + "?" + PARAM_PAGINGACTION + "=" + SearchBundle.getId()
                    + "&" + PARAM_PAGINGOFFSET + "=" + offset
                    + "&" + PARAM_COUNT + "=" + pageSize
                    + "&_bundletype=searchset";

            // Load the required page
            return fhirClient.search().byUrl(pageUrl)
                    .returnBundle(Bundle.class)
                    .execute();
        } else {
            throw new ResourceNotFoundException("No resources were found in the FHIR server for the page number: " + pageNumber);
        }
    }

    public static PageDto<?> applyPaginationForSearchBundle(List<?> elements,
                                                            int totalElementsInBundle,
                                                            int numberOfElementsPerPage,
                                                            Optional<Integer> pageNumber) {
        boolean firstPage = isFirstPage(pageNumber);
        double totalPages = Math.ceil((double) totalElementsInBundle / numberOfElementsPerPage);
        int currentPage = firstPage ? 1 : pageNumber.get();
        return new PageDto<>(elements, numberOfElementsPerPage, totalPages, currentPage, elements.size(), totalElementsInBundle);
    }

    public static PageDto<?> applyPaginationForCustomArrayList(List<?> elements,
                                                               int numberOfElementsPerPage,
                                                               Optional<Integer> pageNumber,
                                                               boolean throwExceptionWhenResourceNotFound) {
        boolean firstPage = isFirstPage(pageNumber);
        int currentPage = firstPage ? 1 : pageNumber.get(); // Assuming page number starts with 1

        if (elements == null || elements.isEmpty()) {
            if (throwExceptionWhenResourceNotFound) {
                throw new ResourceNotFoundException("No resources found!");
            } else {
                return new PageDto<>(new ArrayList<>(), numberOfElementsPerPage, 0, 0, 0, 0);
            }
        }

        int totalElements = elements.size();
        double totalPages = Math.ceil((double) totalElements / numberOfElementsPerPage);

        // Check validity of the page number
        if (currentPage > totalPages) {
            throw new ResourceNotFoundException("No resources were found in the FHIR server for the page number: " + currentPage);
        }

        int startIndex = ((currentPage - 1) * numberOfElementsPerPage);
        int endIndex = (currentPage * numberOfElementsPerPage) - 1;
        int lastElementIndex = totalElements - 1;

        // Just to be doubly sure
        if (startIndex > lastElementIndex) {
            throw new ResourceNotFoundException("Something is off about the page number you are requesting! ");
        }

        List<?> currentPageElements;
        if (endIndex > lastElementIndex) {
            currentPageElements = elements.subList(startIndex, ++lastElementIndex);
        } else {
            currentPageElements = elements.subList(startIndex, ++endIndex);
        }
        return new PageDto<>(currentPageElements, numberOfElementsPerPage, totalPages, currentPage, currentPageElements.size(), totalElements);
    }

    public static boolean isFirstPage(Optional<Integer> pageNumber) {
        boolean firstPage = true;
        if (pageNumber.isPresent() && pageNumber.get() > 1) {
            firstPage = false;
        }
        return firstPage;
    }

    public static int getValidPageSize(ConfigProperties configProperties, Optional<Integer> pageSize, String resource) {
        int numberOfResourcesPerPage;

        switch (resource.toUpperCase()) {
            case "ORGANIZATION":
                numberOfResourcesPerPage = pageSize.filter(s -> s > 0 &&
                        s <= configProperties.getOrganization().getPagination().getMaxSize()).orElse(configProperties.getOrganization().getPagination().getDefaultSize());
                break;
            case "PRACTITIONER":
                numberOfResourcesPerPage = pageSize.filter(s -> s > 0 &&
                        s <= configProperties.getPractitioner().getPagination().getMaxSize()).orElse(configProperties.getPractitioner().getPagination().getDefaultSize());
                break;
            default:
                numberOfResourcesPerPage = pageSize.filter(s -> s > 0 &&
                        s <= configProperties.getResourceSinglePageLimit()).orElse(configProperties.getResourceSinglePageLimit());
        }
        return numberOfResourcesPerPage;
    }
}
