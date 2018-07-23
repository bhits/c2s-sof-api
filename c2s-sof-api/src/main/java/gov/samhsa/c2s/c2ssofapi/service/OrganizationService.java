package gov.samhsa.c2s.c2ssofapi.service;

import gov.samhsa.c2s.c2ssofapi.service.dto.OrganizationDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.web.OrganizationController;

import java.util.List;
import java.util.Optional;

public interface OrganizationService {

    OrganizationDto getOrganization(String organizationId);

    PageDto<OrganizationDto> getAllOrganizations(Optional<Boolean> showInactive, Optional<Integer> page, Optional<Integer> size);

    PageDto<OrganizationDto> searchOrganizations(Optional<OrganizationController.SearchType> searchType, Optional<String> searchValue, Optional<Boolean> showInactive, Optional<Integer> page, Optional<Integer> size, Optional<Boolean> showAll);

    List<OrganizationDto> getOrganizationsByPractitionerId(String practitionerId);
}
