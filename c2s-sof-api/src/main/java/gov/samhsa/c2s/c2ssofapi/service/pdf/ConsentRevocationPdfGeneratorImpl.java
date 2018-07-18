package gov.samhsa.c2s.c2ssofapi.service.pdf;


import gov.samhsa.c2s.c2ssofapi.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class ConsentRevocationPdfGeneratorImpl implements ConsentRevocationPdfGenerator {

    private static final String CONSENT_REVOCATION_PDF = "consent-revocation-pdf";
    private final ConsentPdfGenerator consentPdfGenerator;

    private final String CONSENT_REVOCATION_TERM = "I have previously signed a patient consent form allowing my providers to access my electronic health records\n" +
            "through the Consent2Share system and now want to withdraw that consent. If I sign this form as the Patient's\n" +
            "Legal Representative, I understand that all references in this form to \"me\" or \"my\" refer to the Patient.\n" +
            "\nBy withdrawing my Consent, I understand that:\n\n" +
            "1. I Deny Consent for all Participants to access my electronic health information through Consent2Share for any\n" +
            "purpose, EXCEPT in a medical emergency.\n" +
            "2. Health care provider and health insurers that I am enrolled with will no longer be able to access health\n" +
            "information about me through Consent2Share, except in an emergency.\n" +
            "3. The Withdrawal of Consent will not affect the exchange of my health information while my Consent was in\n" +
            "effect.\n" +
            "4. No Consent2Share participating provider will deny me medical care and my insurance eligibility will not be\n" +
            "affected based on my Withdrawal of Consent.\n" +
            "5. If I wish to reinstate Consent, I may do so by signing and completing a new Patient Consent form and\n" +
            "returning it to a participating provider or payer.\n" +
            "6. Revoking my Consent does not prevent my health care provider from submitting claims to my health insurer\n" +
            "for reimbursement for services rendered to me in reliance on the Consent while it was in effect.\n" +
            "7. I understand that I will get a copy of this form after I sign it.";

    private HexPDF document;

    @Autowired
    public ConsentRevocationPdfGeneratorImpl(ConsentPdfGenerator consentPdfGenerator) {
        this.consentPdfGenerator = consentPdfGenerator;
    }


    @Override
    public byte[] generateConsentRevocationPdf(DetailedConsentDto detailedConsent, PatientDto patient, Boolean revokedByPatient, Optional<String> signatureDataUrl) throws IOException {

        Assert.notNull(detailedConsent, "Consent is required.");

        document = new HexPDF();

        String consentTitle = consentPdfGenerator.getConsentTitle(CONSENT_REVOCATION_PDF);

        consentPdfGenerator.setPageFooter(document, "");

        // Create the first page
        document.newPage();

        // Set document title
        consentPdfGenerator.drawConsentTitle(document, consentTitle);

        // Typeset everything else in boring black
        document.setTextColor(Color.black);

        document.normalStyle();

        consentPdfGenerator.drawPatientInformationSection(document, detailedConsent, patient);

        document.drawText("\n");

        document.drawText(CONSENT_REVOCATION_TERM);

        document.drawText("\n\n");

        consentPdfGenerator.addConsentSigningDetails(document, patient, revokedByPatient);

        signatureDataUrl.ifPresent(s -> drawSignature(document, s));

        // Get the document
        return document.getDocumentAsBytArray();
    }

    private void drawSignature(HexPDF document, String signatureDataUrl) {
        BufferedImage basemap = decodeToImage(signatureDataUrl);
        ImageResizer imageResizer = new ImageResizer();

        try {
            BufferedImage resizedImage = imageResizer.resize(basemap, 0.25);
            document.drawImage(resizedImage, HexPDF.LEFT);

        } catch (IOException e) {
            System.out.println("Error resizing the image.");
            e.printStackTrace();
        }
    }

    private BufferedImage decodeToImage(String imageString) {

        BufferedImage image = null;
        try {
            String encodingPrefix = "base64,";
            int contentStartIndex = imageString.indexOf(encodingPrefix) + encodingPrefix.length();
            byte[] imageData = Base64.decodeBase64(imageString.substring(contentStartIndex));

            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }


}
