package gov.samhsa.c2s.c2ssofapi.web;

import gov.samhsa.c2s.c2ssofapi.service.PatientService;
import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    PatientService patientService;

    @GetMapping("/{patientId}")
    public PatientDto getPatientById(@RequestHeader("Authorization") String token,@PathVariable String patientId) {
        return patientService.getPatientById(patientId, Optional.of(token));
    }
}
