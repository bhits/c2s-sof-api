package gov.samhsa.c2s.c2ssofapi.service;


import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;

public interface PatientService {

    PatientDto getPatientById(String patientId, String token);
}
