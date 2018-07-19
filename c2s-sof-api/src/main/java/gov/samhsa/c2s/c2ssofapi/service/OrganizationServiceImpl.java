package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.OrganizationDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.ReferenceDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.OrganizationNotFoundException;
import gov.samhsa.c2s.c2ssofapi.service.util.FhirDtoUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.FhirUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.PaginationUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.RichStringClientParam;
import gov.samhsa.c2s.c2ssofapi.web.OrganizationController;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.PractitionerRole;
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
    private final ConfigProperties configProperties;

    @Autowired
    public OrganizationServiceImpl(ModelMapper modelMapper, IGenericClient fhirClient, ConfigProperties configProperties) {
        this.modelMapper = modelMapper;
        this.fhirClient = fhirClient;
        this.configProperties = configProperties;
    }

    @Override
    public OrganizationDto getOrganization(String organizationId) {
        final Organization retrievedOrganization = fhirClient.read().resource(Organization.class).withId(organizationId).execute();
        if (retrievedOrganization == null || retrievedOrganization.isEmpty()) {
            throw new OrganizationNotFoundException("No organizations were found in the FHIR server.");
        }
        final OrganizationDto organizationDto = modelMapper.map(retrievedOrganization, OrganizationDto.class);
        organizationDto.setLogicalId(retrievedOrganization.getIdElement().getIdPart());
        return organizationDto;
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

    @Override
    public List<ReferenceDto> getOrganizationsByPractitionerId(String practitionerId) {
        List<ReferenceDto> organizations = new ArrayList<>();
        Bundle bundle = fhirClient.search().forResource(PractitionerRole.class)
                .where(new ReferenceClientParam("practitioner").hasId(ResourceType.Practitioner + "/" + practitionerId))
                .include(PractitionerRole.INCLUDE_ORGANIZATION)
                .sort().descending(PARAM_LASTUPDATED)
                .returnBundle(Bundle.class).execute();
        if (bundle != null) {
            List<Bundle.BundleEntryComponent> organizationComponents = FhirUtil.getAllBundleComponentsAsList(bundle, Optional.empty(), fhirClient, configProperties);
            if (organizationComponents != null) {
                organizations = organizationComponents.stream()
                        .filter(it -> it.getResource().getResourceType().equals(ResourceType.PractitionerRole))
                        .map(it -> (PractitionerRole) it.getResource())
                        .map(it -> (Organization) it.getOrganization().getResource())
                        .map(FhirDtoUtil::mapOrganizationToReferenceDto)
                        .distinct()
                        .collect(toList());
            }
        }
        return organizations;
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
