package gov.samhsa.c2s.c2ssofapi.service;

import gov.samhsa.c2s.c2ssofapi.service.dto.PractitionerDto;

public interface
PractitionerService {
    PractitionerDto getPractitioner(String practitionerId);

}
