package gov.samhsa.c2s.c2ssofapi.web;

import gov.samhsa.c2s.c2ssofapi.service.PractitionerService;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PractitionerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/practitioners")
public class PractitionerController {
    @Autowired
    private PractitionerService practitionerService;

    @GetMapping("/{practitionerId}")
    public PractitionerDto getPractitioner(@PathVariable String practitionerId) {
        return practitionerService.getPractitioner(practitionerId);
    }

    @GetMapping("/search")
    public PageDto<PractitionerDto> searchPractitioners(@RequestParam Optional<SearchType> searchType, @RequestParam Optional<String> searchValue, Optional<String> organization, @RequestParam Optional<Boolean> showInactive, @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size, Optional<Boolean> showAll) {
        return practitionerService.searchPractitioners(searchType, searchValue, organization, showInactive, page, size, showAll);
    }

    @GetMapping
    public PageDto<PractitionerDto> getPractitioners(@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
        return practitionerService.getPractitioners(page, size);
    }

    public enum SearchType {
        identifier, name
    }
}
