package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.validation.FhirValidator;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.OrganizationDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.FHIRClientException;
import gov.samhsa.c2s.c2ssofapi.service.exception.OrganizationNotFoundException;
import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;
import gov.samhsa.c2s.c2ssofapi.service.util.FhirUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.PaginationUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.RichStringClientParam;
import gov.samhsa.c2s.c2ssofapi.web.OrganizationController;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ca.uhn.fhir.rest.api.Constants.PARAM_LASTUPDATED;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {


    private final ModelMapper modelMapper;
    private final IGenericClient fhirClient;
    private final FhirValidator fhirValidator;
    private final ConfigProperties configProperties;

    private final LookUpService lookUpService;

    @Autowired
    public OrganizationServiceImpl(ModelMapper modelMapper, IGenericClient fhirClient, FhirValidator fhirValidator, ConfigProperties configProperties, LookUpService lookUpService) {
        this.modelMapper = modelMapper;
        this.fhirClient = fhirClient;
        this.fhirValidator = fhirValidator;
        this.configProperties = configProperties;
        this.lookUpService = lookUpService;
    }

    @Override
    public PageDto<OrganizationDto> getAllOrganizations(Optional<Boolean> showInactive, Optional<Integer> page, Optional<Integer> size) {
        int numberOfOrganizationsPerPage = PaginationUtil.getValidPageSize(configProperties, size, ResourceType.Organization.name());

        IQuery organizationIQuery = fhirClient.search().forResource(Organization.class);

        //Set Sort order
        organizationIQuery = FhirUtil.setLastUpdatedTimeSortOrder(organizationIQuery, true);

        if (showInactive.isPresent()) {
            if (!showInactive.get())
                organizationIQuery.where(new TokenClientParam("active").exactly().code("true"));
        } else {
            organizationIQuery.where(new TokenClientParam("active").exactly().code("true"));
        }

        Bundle firstPageOrganizationSearchBundle;
        Bundle otherPageOrganizationSearchBundle;
        boolean firstPage = true;

        firstPageOrganizationSearchBundle = PaginationUtil.getSearchBundleFirstPage(organizationIQuery, numberOfOrganizationsPerPage, Optional.empty());

        if (firstPageOrganizationSearchBundle == null || firstPageOrganizationSearchBundle.getEntry().size() < 1) {
            log.info("No organizations were found for the given criteria.");
            return new PageDto<>(new ArrayList<>(), numberOfOrganizationsPerPage, 0, 0, 0, 0);
        }

        otherPageOrganizationSearchBundle = firstPageOrganizationSearchBundle;

        if (page.isPresent() && page.get() > 1 && otherPageOrganizationSearchBundle.getLink(Bundle.LINK_NEXT) != null) {
            firstPage = false;
            // Load the required page
            otherPageOrganizationSearchBundle = PaginationUtil.getSearchBundleAfterFirstPage(fhirClient, configProperties, firstPageOrganizationSearchBundle, page.get(), numberOfOrganizationsPerPage);
        }

        List<Bundle.BundleEntryComponent> retrievedOrganizations = otherPageOrganizationSearchBundle.getEntry();

        List<OrganizationDto> organizationsList = retrievedOrganizations.stream().map(retrievedOrganization -> {
            OrganizationDto organizationDto = modelMapper.map(retrievedOrganization.getResource(), OrganizationDto.class);
            organizationDto.setLogicalId(retrievedOrganization.getResource().getIdElement().getIdPart());
            return organizationDto;
        }).collect(toList());

        double totalPages = Math.ceil((double) otherPageOrganizationSearchBundle.getTotal() / numberOfOrganizationsPerPage);
        int currentPage = firstPage ? 1 : page.get();

        return new PageDto<>(organizationsList, numberOfOrganizationsPerPage, totalPages, currentPage, organizationsList.size(), otherPageOrganizationSearchBundle.getTotal());
    }

    @Override
    public PageDto<OrganizationDto> searchOrganizations(Optional<OrganizationController.SearchType> type, Optional<String> value, Optional<Boolean> showInactive, Optional<Integer> page, Optional<Integer> size, Optional<Boolean> showAll) {
        int numberOfOrganizationsPerPage = PaginationUtil.getValidPageSize(configProperties, size, ResourceType.Organization.name());

        IQuery organizationIQuery = fhirClient.search().forResource(Organization.class).sort().descending(PARAM_LASTUPDATED);

        type.ifPresent(t -> {
                    if (t.equals(OrganizationController.SearchType.name))
                        value.ifPresent(v -> organizationIQuery.where(new RichStringClientParam("name").contains().value(v.trim())));

                    if (t.equals(OrganizationController.SearchType.identifier))
                        value.ifPresent(v -> organizationIQuery.where(new TokenClientParam("identifier").exactly().code(v)));

                    if (t.equals(OrganizationController.SearchType.logicalId))
                        value.ifPresent(v -> organizationIQuery.where(new TokenClientParam("_id").exactly().code(v)));
                }
        );

        if (showInactive.isPresent()) {
            if (!showInactive.get())
                organizationIQuery.where(new TokenClientParam("active").exactly().code("true"));
        } else {
            organizationIQuery.where(new TokenClientParam("active").exactly().code("true"));
        }

        Bundle firstPageOrganizationSearchBundle;
        Bundle otherPageOrganizationSearchBundle;
        boolean firstPage = true;

        firstPageOrganizationSearchBundle = (Bundle) organizationIQuery.count(numberOfOrganizationsPerPage).returnBundle(Bundle.class)
                .execute();

        if (showAll.isPresent() && showAll.get()) {
            List<OrganizationDto> organizationDtos = convertAllBundleToSingleOrganizationDtoList(firstPageOrganizationSearchBundle, numberOfOrganizationsPerPage);
            return (PageDto<OrganizationDto>) PaginationUtil.applyPaginationForCustomArrayList(organizationDtos, organizationDtos.size(), Optional.of(1), false);
        }

        if (firstPageOrganizationSearchBundle == null || firstPageOrganizationSearchBundle.isEmpty() || firstPageOrganizationSearchBundle.getEntry().size() < 1) {
            throw new OrganizationNotFoundException("No organizations were found in the FHIR server.");
        }

        otherPageOrganizationSearchBundle = firstPageOrganizationSearchBundle;

        if (page.isPresent() && page.get() > 1 && otherPageOrganizationSearchBundle.getLink(Bundle.LINK_NEXT) != null) {
            firstPage = false;

            otherPageOrganizationSearchBundle = PaginationUtil.getSearchBundleAfterFirstPage(fhirClient, configProperties, firstPageOrganizationSearchBundle, page.get(), numberOfOrganizationsPerPage);
        }

        List<Bundle.BundleEntryComponent> retrievedOrganizations = otherPageOrganizationSearchBundle.getEntry();

        List<OrganizationDto> organizationsList = retrievedOrganizations.stream().map(retrievedOrganization -> {
            OrganizationDto organizationDto = modelMapper.map(retrievedOrganization.getResource(), OrganizationDto.class);
            organizationDto.setLogicalId(retrievedOrganization.getResource().getIdElement().getIdPart());
            return organizationDto;
        }).collect(toList());

        double totalPages = Math.ceil((double) otherPageOrganizationSearchBundle.getTotal() / numberOfOrganizationsPerPage);
        int currentPage = firstPage ? 1 : page.get();

        return new PageDto<>(organizationsList, numberOfOrganizationsPerPage, totalPages, currentPage, organizationsList.size(), otherPageOrganizationSearchBundle.getTotal());
    }

    private int getOrganizationsCountByIdentifier(String system, String code) {
        log.info("Searching organizations with identifier.system : " + system + " and code : " + code);

        Bundle searchBundle = fhirClient.search().forResource(Organization.class).where(new TokenClientParam("identifier").exactly().systemAndCode(system, code))
                .returnBundle(Bundle.class).execute();
        if (searchBundle.getTotal() == 0) {
            Bundle organizationBundle = fhirClient.search().forResource(Organization.class).returnBundle(Bundle.class).execute();
            return FhirUtil.getAllBundleComponentsAsList(organizationBundle, Optional.empty(), fhirClient, configProperties).stream().filter(organization -> {
                Organization o = (Organization) organization.getResource();
                return o.getIdentifier().stream().anyMatch(identifier ->
                        (identifier.getSystem().equalsIgnoreCase(system) &&
                                identifier.getValue().replaceAll(" ", "")
                                        .replaceAll("-", "").trim()
                                        .equalsIgnoreCase(code.replaceAll(" ", "").replaceAll("-", "").trim()))
                );
            }).collect(toList()).size();
        } else {
            return searchBundle.getTotal();
        }
    }

    private Organization readOrganizationFromServer(String organizationId) {
        Organization existingFhirOrganization;

        try {
            existingFhirOrganization = fhirClient.read().resource(Organization.class).withId(organizationId.trim()).execute();
        } catch (BaseServerResponseException e) {
            log.error("FHIR Client returned with an error while reading the organization with ID: " + organizationId);
            throw new ResourceNotFoundException("FHIR Client returned with an error while reading the organization:" + e.getMessage());
        }
        return existingFhirOrganization;
    }

    private void setOrganizationStatusToInactive(Organization existingFhirOrganization) {
        existingFhirOrganization.setActive(false);
        try {
            MethodOutcome serverResponse = fhirClient.update().resource(existingFhirOrganization).execute();
            log.info("Inactivated the organization :" + serverResponse.getId().getIdPart());
        } catch (BaseServerResponseException e) {
            log.error("Could NOT inactivate organization");
            throw new FHIRClientException("FHIR Client returned with an error while inactivating the organization:" + e.getMessage());
        }
    }

    private List<OrganizationDto> convertAllBundleToSingleOrganizationDtoList(Bundle firstPageOrganizationSearchBundle, int numberOBundlePerPage) {
        return FhirUtil.getAllBundleComponentsAsList(firstPageOrganizationSearchBundle, Optional.of(numberOBundlePerPage), fhirClient, configProperties)
                .stream()
                .map(retrievedOrganization -> {
                    OrganizationDto organizationDto = modelMapper.map(retrievedOrganization.getResource(), OrganizationDto.class);
                    organizationDto.setLogicalId(retrievedOrganization.getResource().getIdElement().getIdPart());
                    return organizationDto;
                })
                .collect(toList());
    }

}
