package gov.samhsa.c2s.c2ssofapi.service;

import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PractitionerDto;
import gov.samhsa.c2s.c2ssofapi.web.PractitionerController;

import java.util.Optional;

public interface
PractitionerService {
    PractitionerDto getPractitioner(String practitionerId);

    PageDto<PractitionerDto> searchPractitioners(Optional<PractitionerController.SearchType> type, Optional<String> value, Optional<String> organization, Optional<Boolean> showInactive, Optional<Integer> page, Optional<Integer> size, Optional<Boolean> showAll);

    PageDto<PractitionerDto> getPractitioners(Optional<Integer> pageNumber, Optional<Integer> pageSize);
}
