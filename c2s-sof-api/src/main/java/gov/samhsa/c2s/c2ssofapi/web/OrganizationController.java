package gov.samhsa.c2s.c2ssofapi.web;

import gov.samhsa.c2s.c2ssofapi.service.OrganizationService;
import gov.samhsa.c2s.c2ssofapi.service.dto.OrganizationDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {
    public enum SearchType {
        identifier, name, logicalId
    }

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("/{organizationId}")
    public OrganizationDto getOrganization(@PathVariable String organizationId) {
        return organizationService.getOrganization(organizationId);
    }

    // Todo: Resolve endpoint conflicts with getOrganizationsByPractitioner
    @GetMapping("all")
    public PageDto<OrganizationDto> getOrganizations(@RequestParam Optional<Boolean> showInactive, @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
        return organizationService.getAllOrganizations(showInactive, page, size);
    }

    @GetMapping("/search")
    public PageDto<OrganizationDto> searchOrganizations(@RequestParam Optional<SearchType> searchType, @RequestParam Optional<String> searchValue, @RequestParam Optional<Boolean> showInactive, @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size, Optional<Boolean> showAll) {
        return organizationService.searchOrganizations(searchType, searchValue, showInactive, page, size, showAll);
    }

    @GetMapping
    public List<OrganizationDto> getOrganizationsByPractitionerId(@RequestParam(value = "practitionerId") String practitionerId) {
        return organizationService.getOrganizationsByPractitionerId(practitionerId);
    }
}
