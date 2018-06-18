package gov.samhsa.c2s.c2ssofapi.service;


import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;

import java.util.Optional;

public interface PatientService {

    PatientDto getPatientById(String patientId, Optional<String> token);
}
