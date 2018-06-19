package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PractitionerDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PractitionerRoleDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;
import gov.samhsa.c2s.c2ssofapi.service.util.FhirUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.PaginationUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.RichStringClientParam;
import gov.samhsa.c2s.c2ssofapi.web.PractitionerController;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Practitioner;
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
public class PractitionerServiceImpl implements PractitionerService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private ConfigProperties configProperties;

    @Override
    public PractitionerDto getPractitioner(String practitionerId) {
        Bundle practitionerBundle = fhirClient.search().forResource(Practitioner.class)
                .where(new TokenClientParam("_id").exactly().code(practitionerId))
                .revInclude(PractitionerRole.INCLUDE_PRACTITIONER)
                .sort().descending(PARAM_LASTUPDATED)
                .returnBundle(Bundle.class)
                .execute();

        if (practitionerBundle == null || practitionerBundle.getEntry().size() < 1) {
            throw new ResourceNotFoundException("No practitioner was found for the givecn practitionerID:" + practitionerId);
        }

        List<Bundle.BundleEntryComponent> retrievedPractitioners = practitionerBundle.getEntry();
        Bundle.BundleEntryComponent retrievedPractitioner = practitionerBundle.getEntry().get(0);

        PractitionerDto practitionerDto = modelMapper.map(retrievedPractitioner.getResource(), PractitionerDto.class);
        practitionerDto.setLogicalId(retrievedPractitioner.getResource().getIdElement().getIdPart());

        return practitionerDto;
    }

    private List<PractitionerRoleDto> getPractitionerRolesForEachPractitioner(List<Bundle.BundleEntryComponent> practitionersWithAllReferenceBundle, String practitionerId) {
        return practitionersWithAllReferenceBundle.stream()
                .filter(practitionerWithAllReference -> practitionerWithAllReference.getResource().getResourceType().equals(ResourceType.PractitionerRole))
                .map(practitionerRoleBundle -> (PractitionerRole) practitionerRoleBundle.getResource())
                .filter(practitionerRole -> practitionerRole.getPractitioner().getReference().equalsIgnoreCase("Practitioner/" + practitionerId))
                .map(practitionerRole -> {
                    PractitionerRoleDto practitionerRoleDto;
                    practitionerRoleDto = modelMapper.map(practitionerRole, PractitionerRoleDto.class);
                    practitionerRoleDto.setLogicalId(practitionerRole.getIdElement().getIdPart());
                    return practitionerRoleDto;
                }).collect(toList());
    }

    @Override
    public PageDto<PractitionerDto> searchPractitioners(Optional<PractitionerController.SearchType> type, Optional<String> value, Optional<String> organization, Optional<Boolean> showInactive, Optional<Integer> page, Optional<Integer> size, Optional<Boolean> showAll) {
        int numberOfPractitionersPerPage = PaginationUtil.getValidPageSize(configProperties, size, ResourceType.Practitioner.name());
        IQuery practitionerIQuery = fhirClient.search().forResource(Practitioner.class).sort().descending(PARAM_LASTUPDATED);


        boolean firstPage = true;
        type.ifPresent(t -> {
                    if (t.equals(PractitionerController.SearchType.name))
                        value.ifPresent(v -> practitionerIQuery.where(new RichStringClientParam("name").contains().value(v.trim())));

                    if (t.equals(PractitionerController.SearchType.identifier))
                        value.ifPresent(v -> practitionerIQuery.where(new TokenClientParam("identifier").exactly().code(v.trim())));
                }
        );

        if (showInactive.isPresent()) {
            if (!showInactive.get())
                practitionerIQuery.where(new TokenClientParam("active").exactly().code("true"));
        } else {
            practitionerIQuery.where(new TokenClientParam("active").exactly().code("true"));
        }

        Bundle firstPagePractitionerSearchBundle;
        Bundle otherPagePractitionerSearchBundle;

        firstPagePractitionerSearchBundle = (Bundle) practitionerIQuery.count(numberOfPractitionersPerPage)
                .revInclude(PractitionerRole.INCLUDE_PRACTITIONER)
                .returnBundle(Bundle.class)
                .execute();

        if (showAll.isPresent() && showAll.get()) {
            List<PractitionerDto> patientDtos = convertAllBundleToSinglePractitionerDtoList(firstPagePractitionerSearchBundle, numberOfPractitionersPerPage);
            return (PageDto<PractitionerDto>) PaginationUtil.applyPaginationForCustomArrayList(patientDtos, patientDtos.size(), Optional.of(1), false);
        }

        if (firstPagePractitionerSearchBundle == null || firstPagePractitionerSearchBundle.isEmpty() || firstPagePractitionerSearchBundle.getEntry().size() < 1) {
            return new PageDto<>(new ArrayList<>(), numberOfPractitionersPerPage, 0, 0, 0, 0);
        }

        otherPagePractitionerSearchBundle = firstPagePractitionerSearchBundle;

        if (page.isPresent() && page.get() > 1 && otherPagePractitionerSearchBundle.getLink(Bundle.LINK_NEXT) != null) {
            firstPage = false;
            otherPagePractitionerSearchBundle = PaginationUtil.getSearchBundleAfterFirstPage(fhirClient, configProperties, firstPagePractitionerSearchBundle, page.get(), numberOfPractitionersPerPage);
        }

        List<Bundle.BundleEntryComponent> retrievedPractitioners = otherPagePractitionerSearchBundle.getEntry();

        return practitionersInPage(retrievedPractitioners, otherPagePractitionerSearchBundle, numberOfPractitionersPerPage, firstPage, page);
    }

    private List<PractitionerDto> convertAllBundleToSinglePractitionerDtoList(Bundle firstPageSearchBundle, int numberOfBundlePerPage) {
        List<Bundle.BundleEntryComponent> bundleEntryComponents = FhirUtil.getAllBundleComponentsAsList(firstPageSearchBundle, Optional.of(numberOfBundlePerPage), fhirClient, configProperties);
        return bundleEntryComponents.stream().filter(pr -> pr.getResource().getResourceType().equals(ResourceType.Practitioner))
                .map(prac -> this.covertEntryComponentToPractitioner(prac, bundleEntryComponents)).collect(toList());
    }

    private PractitionerDto covertEntryComponentToPractitioner(Bundle.BundleEntryComponent practitionerComponent, List<Bundle.BundleEntryComponent> practitionerAndPractitionerRoleList) {
        PractitionerDto practitionerDto = modelMapper.map(practitionerComponent.getResource(), PractitionerDto.class);
        practitionerDto.setLogicalId(practitionerComponent.getResource().getIdElement().getIdPart());
        return practitionerDto;
    }

    private PageDto<PractitionerDto> practitionersInPage(List<Bundle.BundleEntryComponent> retrievedPractitioners, Bundle otherPagePractitionerBundle, int numberOfPractitionersPerPage, boolean firstPage, Optional<Integer> page) {
        List<PractitionerDto> practitionersList = retrievedPractitioners.stream()
                .filter(retrievedPractitionerAndPractitionerRoles -> retrievedPractitionerAndPractitionerRoles.getResource().getResourceType().equals(ResourceType.Practitioner))
                .map(retrievedPractitioner -> covertEntryComponentToPractitioner(retrievedPractitioner, retrievedPractitioners))
                .collect(toList());

        double totalPages = Math.ceil((double) otherPagePractitionerBundle.getTotal() / numberOfPractitionersPerPage);
        int currentPage = firstPage ? 1 : page.get();

        return new PageDto<>(practitionersList, numberOfPractitionersPerPage, totalPages, currentPage, practitionersList.size(),
                otherPagePractitionerBundle.getTotal());
    }


    @Override
    public PageDto<PractitionerDto> getPractitionersByOrganizationAndRole(String organization, Optional<String> role, Optional<Integer> pageNumber, Optional<Integer> pageSize) {
        int numberOfPractitionersPerPage = PaginationUtil.getValidPageSize(configProperties, pageSize, ResourceType.Practitioner.name());
        List<PractitionerDto> practitioners = new ArrayList<>();

        IQuery query = fhirClient.search().forResource(PractitionerRole.class).sort().descending(PARAM_LASTUPDATED);

        role.ifPresent(s -> query.where(new TokenClientParam("role").exactly().code(s)));

        List<Bundle.BundleEntryComponent> practitionerEntry = getBundleForPractitioners(organization, query);
        practitioners = getPractitionerDtos(practitioners, practitionerEntry);

        return (PageDto<PractitionerDto>) PaginationUtil.applyPaginationForCustomArrayList(practitioners, numberOfPractitionersPerPage, pageNumber, false);
    }

    private List<PractitionerDto> getPractitionerDtos(List<PractitionerDto> practitioners, List<Bundle.BundleEntryComponent> bundleEntry) {
        if (bundleEntry != null && !bundleEntry.isEmpty()) {
            practitioners = bundleEntry.stream()
                    .filter(it -> it.getResource().getResourceType().equals(ResourceType.Practitioner))
                    .map(it -> (Practitioner) it.getResource())
                    .map(it -> {
                        PractitionerDto practitionerDto = modelMapper.map(it, PractitionerDto.class);
                        practitionerDto.setLogicalId(it.getIdElement().getIdPart());
                        return practitionerDto;
                    }).distinct()
                    .collect(toList());
        }
        return practitioners;
    }

    private List<Bundle.BundleEntryComponent> getBundleForPractitioners(String organization, IQuery query) {
        Bundle practitionerBundle = (Bundle) query.where(new ReferenceClientParam("organization").hasId(organization))
                .include(new Include("PractitionerRole:practitioner"))
                .returnBundle(Bundle.class)
                .execute();
        return FhirUtil.getAllBundleComponentsAsList(practitionerBundle, Optional.empty(), fhirClient, configProperties);
    }

}
