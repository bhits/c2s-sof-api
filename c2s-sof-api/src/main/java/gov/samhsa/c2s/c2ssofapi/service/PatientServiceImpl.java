package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Flag;
import org.hl7.fhir.dstu3.model.Patient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class PatientServiceImpl implements PatientService {

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ConfigProperties configProperties;

    private PatientDto mapPatientToPatientDto(Patient patient, List<Bundle.BundleEntryComponent> response) {
        PatientDto patientDto = modelMapper.map(patient, PatientDto.class);
        patientDto.setId(patient.getIdElement().getIdPart());

        if (patient.getGender() != null)
            patientDto.setGenderCode(patient.getGender().toCode());
        return patientDto;
    }

    @Override
    public PatientDto getPatientById(String patientId, String token) {

        Bundle patientBundle = fhirClient.search().forResource(Patient.class)
                .where(new TokenClientParam("_id").exactly().code(patientId))
                .revInclude(Flag.INCLUDE_PATIENT)
                .returnBundle(Bundle.class)
                .execute();

        if (patientBundle == null || patientBundle.getEntry().size() < 1) {
            throw new ResourceNotFoundException("No patient was found for the given patientID : " + patientId);
        }

        Bundle.BundleEntryComponent patientBundleEntry = patientBundle.getEntry().get(0);
        Patient patient = (Patient) patientBundleEntry.getResource();
        PatientDto patientDto = modelMapper.map(patient, PatientDto.class);
        patientDto.setId(patient.getIdElement().getIdPart());
        patientDto.setBirthDate(patient.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        patientDto.setGenderCode(patient.getGender().toCode());

        return patientDto;
    }
}


