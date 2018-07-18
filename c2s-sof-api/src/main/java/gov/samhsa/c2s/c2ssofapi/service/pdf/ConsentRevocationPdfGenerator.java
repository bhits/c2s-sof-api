package gov.samhsa.c2s.c2ssofapi.service.pdf;

import gov.samhsa.c2s.c2ssofapi.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;

import java.io.IOException;
import java.util.Optional;

public interface ConsentRevocationPdfGenerator {

    byte[] generateConsentRevocationPdf(DetailedConsentDto detailedConsent, PatientDto patient, Boolean revokedByPatient, Optional<String> signatureDataUrl) throws IOException;

}
