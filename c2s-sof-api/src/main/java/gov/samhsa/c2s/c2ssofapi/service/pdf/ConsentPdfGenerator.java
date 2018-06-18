package gov.samhsa.c2s.c2ssofapi.service.pdf;


import gov.samhsa.c2s.c2ssofapi.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;

import java.io.IOException;

public interface ConsentPdfGenerator {
    String getConsentTitle(String pdfType);

    void drawConsentTitle(HexPDF document, String consentTitle);

    void setPageFooter(HexPDF document, String consentTitle);

    void drawPatientInformationSection(HexPDF document, DetailedConsentDto detailedConsentDto, PatientDto patientDto);

    void addConsentSigningDetails(HexPDF document, PatientDto patient, Boolean signedByPatient) throws IOException;

    byte[] generateConsentPdf(DetailedConsentDto detailedConsent, PatientDto patientProfile, Boolean operatedByPatient) throws IOException;

}
