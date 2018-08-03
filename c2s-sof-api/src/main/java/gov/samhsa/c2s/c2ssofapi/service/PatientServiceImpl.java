package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.IdentifierDto;
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
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final IGenericClient fhirClient;

    private final ModelMapper modelMapper;

    private final ConfigProperties configProperties;

    @Autowired
    public PatientServiceImpl(IGenericClient fhirClient, ModelMapper modelMapper, ConfigProperties configProperties) {
        this.fhirClient = fhirClient;
        this.modelMapper = modelMapper;
        this.configProperties = configProperties;
    }

    @Override
    public PatientDto getPatientById(String patientId, Optional<String> token) {

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
        patientDto.setMrn(patientDto.getIdentifier().stream().filter(iden -> iden.getOid().equalsIgnoreCase(configProperties.getPatient().getMrn().getCodeSystemOID()) || iden.getSystemDisplay().equalsIgnoreCase(configProperties.getPatient().getMrn().getDisplayName())).findFirst().map(IdentifierDto::getValue));
        patientDto.setIdentifier(patientDto.getIdentifier().stream().filter(iden -> !iden.getSystem().equalsIgnoreCase(configProperties.getPatient().getMrn().getCodeSystemOID())).collect(toList()));

        return patientDto;
    }
}


