package gov.samhsa.c2s.c2ssofapi.service.pdf;

import gov.samhsa.c2s.c2ssofapi.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;

import java.io.IOException;

public interface ConsentRevocationPdfGenerator {

    byte[] generateConsentRevocationPdf(DetailedConsentDto detailedConsent, PatientDto patient, Boolean revokedByPatient,String signatureDataUrl) throws IOException;

}
