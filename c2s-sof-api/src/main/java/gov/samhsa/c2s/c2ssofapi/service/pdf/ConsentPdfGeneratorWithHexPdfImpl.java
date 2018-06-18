package gov.samhsa.c2s.c2ssofapi.service.pdf;

import gov.samhsa.c2s.c2ssofapi.config.PdfProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.AbstractCareTeamDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.AddressDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PatientDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.ReferenceDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.NoDataFoundException;
import gov.samhsa.c2s.c2ssofapi.service.exception.PdfConfigMissingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConsentPdfGeneratorWithHexPdfImpl implements ConsentPdfGenerator {
    private static final String DATE_FORMAT_PATTERN = "MMM dd, yyyy";
    private static final String CONSENT_PDF = "consent-pdf";
    private static final String TELECOM_EMAIL = "EMAIL";
    private static final String userNameKey = "ATTESTER_FULL_NAME";
    private static final String SPACE_PATTERN = " ";

    private static final String CONSENT_TERM = "I, " + userNameKey + ", understand that my records are protected under the federal regulations governing Confidentiality of"
            + " Alcohol and Drug Abuse Patient Records, 42 CFR part 2, and cannot be disclosed without my written"
            + " permission or as otherwise permitted by 42 CFR part 2. I also understand that I may revoke this consent at any"
            + " time except to the extent that action has been taken in reliance on it, and that any event this consent expires"
            + " automatically as follows:";

    private static final String NEWLINE_CHARACTER = "\n";
    private static final String NEWLINE_AND_LIST_PREFIX = "\n- ";

    private final PdfProperties pdfProperties;

    @Autowired
    public ConsentPdfGeneratorWithHexPdfImpl(PdfProperties pdfProperties) {
        this.pdfProperties = pdfProperties;
    }

    @Override
    public byte[] generateConsentPdf(DetailedConsentDto detailedConsent, PatientDto patientDto, Boolean operatedByPatient) throws IOException {
        Assert.notNull(detailedConsent, "Consent is required.");

        String consentTitle = getConsentTitle(CONSENT_PDF);

        HexPDF document = new HexPDF();

        setPageFooter(document, "");

        // Create the first page
        document.newPage();

        // Set document title
        drawConsentTitle(document, consentTitle);

        // Typeset everything else in boring black
        document.setTextColor(Color.black);

        document.normalStyle();

        drawPatientInformationSection(document, detailedConsent, patientDto);

        drawAuthorizeToDiscloseSectionTitle(document, detailedConsent);

        drawHealthInformationToBeDisclosedSection(document, detailedConsent);

        drawConsentTermsSection(document, detailedConsent);

        drawEffectiveAndExspireDateSection(document, detailedConsent);

        // Consent signing details
        if (detailedConsent.getStatus().equalsIgnoreCase("Active")) {
            addConsentSigningDetails(document, patientDto, operatedByPatient);
        }

        // Get the document
        return document.getDocumentAsBytArray();
    }

    @Override
    public String getConsentTitle(String pdfType) {
        return pdfProperties.getPdfConfigs().stream()
                .filter(pdfConfig -> pdfConfig.type.equalsIgnoreCase(pdfType))
                .map(PdfProperties.PdfConfig::getTitle)
                .findAny()
                .orElseThrow(PdfConfigMissingException::new);
    }

    @Override
    public void setPageFooter(HexPDF document, String consentTitle) {
        document.setFooter(Footer.defaultFooter);
        // Change center text in footer
        document.getFooter().setCenterText(consentTitle);
        // Use footer also on first page
        document.getFooter().setOMIT_FIRSTPAGE(false);
    }

    public void drawConsentTitle(HexPDF document, String consentTitle) {
        // Add a main title, centered in shiny colours
        document.title1Style();
        document.setTextColor(Color.black);
        document.drawText(consentTitle + NEWLINE_CHARACTER, HexPDF.CENTER);
    }

    @Override
    public void drawPatientInformationSection(HexPDF document, DetailedConsentDto detailedConsent, PatientDto patientDto) {
        String patientFullName = detailedConsent.getPatient().getDisplay();
        String patientBirthDate = formatLocalDate(patientDto.getBirthDate(), DATE_FORMAT_PATTERN);

        Object[][] patientInfo = {
                {NEWLINE_CHARACTER + "Consent Reference Number: " + detailedConsent.getLogicalId(), null},
                {NEWLINE_CHARACTER + "Patient Name: " + patientFullName, NEWLINE_CHARACTER + "Patient DOB: " + patientBirthDate}
        };
        float[] patientInfoTableColumnWidth = new float[]{240, 240};
        int[] patientInfoTableColumnAlignment = new int[]{HexPDF.LEFT, HexPDF.LEFT};

        document.drawTable(patientInfo,
                patientInfoTableColumnWidth,
                patientInfoTableColumnAlignment,
                HexPDF.LEFT);
    }


    private void drawAuthorizeToDiscloseSectionTitle(HexPDF document, DetailedConsentDto detailedConsent) {
        Object[][] title = {
                {"AUTHORIZATION TO DISCLOSE"}
        };
        float[] AuthorizationTitleTableColumnWidth = new float[]{480};
        int[] AuthorizationTitleTableColumnAlignment = new int[]{HexPDF.LEFT};
        document.drawTable(title,
                AuthorizationTitleTableColumnWidth,
                AuthorizationTitleTableColumnAlignment,
                HexPDF.LEFT);
        drawAuthorizationSubSectionHeader(document, NEWLINE_CHARACTER + "Authorizes:" + NEWLINE_CHARACTER);

        if (detailedConsent.isGeneralDesignation()) {
            drawTableWithGeneralDesignation(document, detailedConsent);
        }

        if (detailedConsent.getFromOrganizationActors() != null)
            drawActorsTable(document, detailedConsent.getFromOrganizationActors());

        if (detailedConsent.getFromPractitionerActors() != null)
            drawActorsTable(document, detailedConsent.getFromPractitionerActors());

        if (detailedConsent.getFromRelatedPersons() != null)
            drawActorsTable(document, detailedConsent.getFromRelatedPersons());

        drawAuthorizationSubSectionHeader(document, NEWLINE_CHARACTER + "To disclose to:" + NEWLINE_CHARACTER);

        if (detailedConsent.getToOrganizationActors() != null)
            drawActorsTable(document, detailedConsent.getToOrganizationActors());

        if (detailedConsent.getToPractitionerActors() != null)
            drawActorsTable(document, detailedConsent.getToPractitionerActors());

        if (detailedConsent.getToRelatedPersons() != null)
            drawActorsTable(document, detailedConsent.getToRelatedPersons());

        if (detailedConsent.getToCareTeams() != null)
            drawCareTeamTable(document, detailedConsent.getToCareTeams());

    }


    private void drawAuthorizationSubSectionHeader(HexPDF document, String header) {
        document.title2Style();
        document.drawText(header);
        document.normalStyle();
    }

    private void drawTableWithGeneralDesignation(HexPDF document, DetailedConsentDto consentDto) {
        if (consentDto.isGeneralDesignation()) {
            float[] GeneralDesignationTableColumnWidth = new float[]{480};
            int[] GeneralDesignationTableColumnAlignment = new int[]{HexPDF.LEFT};
            Object[][] generalDesignationText = {{"General Designation Consent"}};
            document.drawTable(generalDesignationText,
                    GeneralDesignationTableColumnWidth,
                    GeneralDesignationTableColumnAlignment,
                    HexPDF.LEFT);
        }

    }

    private void drawActorsTable(HexPDF document, List<AbstractCareTeamDto> actors) {

        Object[][] tableContentsForPractitioners = new Object[actors.size() + 1][5];
        tableContentsForPractitioners[0][0] = "Name";
        tableContentsForPractitioners[0][1] = "Id";
        tableContentsForPractitioners[0][2] = "Identifier";
        tableContentsForPractitioners[0][3] = "Address";
        tableContentsForPractitioners[0][4] = "Phone";


        for (int i = 0; i < actors.size(); i++) {
            tableContentsForPractitioners[i + 1][0] = actors.get(i).getDisplay();
            tableContentsForPractitioners[i + 1][1] = actors.get(i).getId();
            tableContentsForPractitioners[i + 1][2] = actors.get(i).getIdentifiers().get(0).getSystemDisplay().concat(": ").concat(actors.get(i).getIdentifiers().get(0).getValue());
            tableContentsForPractitioners[i + 1][3] = composeAddress(actors.get(i).getAddress());
            tableContentsForPractitioners[i + 1][4] = actors.get(i).getPhoneNumber().orElse("");
        }


        float[] actorTableColumnWidth = new float[]{160, 40, 80, 120, 80};
        int[] providerTableColumnAlignment = new int[]{HexPDF.LEFT, HexPDF.LEFT,HexPDF.LEFT, HexPDF.LEFT, HexPDF.LEFT};

        if (actors.size() > 0)
            document.drawTable(tableContentsForPractitioners,
                    actorTableColumnWidth,
                    providerTableColumnAlignment,
                    HexPDF.LEFT);
    }


    private void drawCareTeamTable(HexPDF document, List<ReferenceDto> actors) {


        Object[][] tableContentsForCareTeams = new Object[actors.size() + 1][3];
        tableContentsForCareTeams[0][0] = "Type";
        tableContentsForCareTeams[0][1] = "Name";
        tableContentsForCareTeams[0][2] = "Id";


        for (int i = 0; i < actors.size(); i++) {
            tableContentsForCareTeams[i + 1][0] = "CareTeam";
            tableContentsForCareTeams[i + 1][1] = actors.get(i).getDisplay();
            tableContentsForCareTeams[i + 1][2] = actors.get(i).getReference().replace("CareTeam/","");
        }


        float[] actorTableColumnWidth = new float[]{160, 160, 160};
        int[] providerTableColumnAlignment = new int[]{HexPDF.LEFT, HexPDF.LEFT, HexPDF.LEFT};

        if (actors.size() > 0)
            document.drawTable(tableContentsForCareTeams,
                    actorTableColumnWidth,
                    providerTableColumnAlignment,
                    HexPDF.LEFT);
    }

    private void drawHealthInformationToBeDisclosedSection(HexPDF document, DetailedConsentDto consentDto) {
        document.drawText(NEWLINE_CHARACTER);

        Object[][] title = {
                {"HEALTH INFORMATION TO BE DISCLOSED"}
        };
        float[] healthInformationTaleWidth = new float[]{480};
        int[] healthInformationTaleAlignment = new int[]{HexPDF.LEFT};
        document.drawTable(title,
                healthInformationTaleWidth,
                healthInformationTaleAlignment,
                HexPDF.LEFT);

        String sensitivityCategoriesLabel = "To SHARE the following medical information:";
        String subLabel = "Sensitivity Categories:";
        String sensitivityCategories = consentDto.getCategory().stream()
                .map(valueSet -> valueSet.getDisplay()).collect(Collectors.joining(NEWLINE_AND_LIST_PREFIX));

        String sensitivityCategoriesStr = sensitivityCategoriesLabel
                .concat(NEWLINE_CHARACTER).concat(subLabel)
                .concat(NEWLINE_AND_LIST_PREFIX).concat(sensitivityCategories);

        String purposeLabel = "To SHARE for the following purpose(s):";

        String purposes = consentDto.getPurpose().stream()
                .map(valueSet -> valueSet.getDisplay()).collect(Collectors.joining(NEWLINE_AND_LIST_PREFIX));
        String purposeOfUseStr = purposeLabel.concat(NEWLINE_AND_LIST_PREFIX).concat(purposes);

        Object[][] healthInformationHeaders = {
                {sensitivityCategoriesStr, purposeOfUseStr}
        };
        float[] healthInformationTableColumnWidth = new float[]{240, 240};
        int[] healthInformationTableColumnAlignment = new int[]{HexPDF.LEFT, HexPDF.LEFT};

        document.drawTable(healthInformationHeaders,
                healthInformationTableColumnWidth,
                healthInformationTableColumnAlignment,
                HexPDF.LEFT);
    }

    private void drawConsentTermsSection(HexPDF document, DetailedConsentDto consentDto) {

        Object[][] title = {
                {"CONSENT TERMS"}
        };
        float[] consentTermsColumnWidth = new float[]{480};
        int[] consentTermsColumnAlignment = new int[]{HexPDF.LEFT};
        document.drawTable(title,
                consentTermsColumnWidth,
                consentTermsColumnAlignment,
                HexPDF.LEFT);

        String termsWithAttestedName = CONSENT_TERM.replace(userNameKey, consentDto.getPatient().getDisplay().toUpperCase());

        document.drawText(termsWithAttestedName);
    }

    private void drawEffectiveAndExspireDateSection(HexPDF document, DetailedConsentDto consent) {
        // Prepare table content
        String effectiveDateContent = "Effective Date: ".concat(formatLocalDate(consent.getPeriod().getStart(), DATE_FORMAT_PATTERN));
        String expirationDateContent = "Expiration Date: ".concat(formatLocalDate(consent.getPeriod().getEnd(), DATE_FORMAT_PATTERN));

        Object[][] title = {
                {effectiveDateContent, expirationDateContent}
        };
        document.drawText(NEWLINE_CHARACTER);
        document.drawText(NEWLINE_CHARACTER);

        float[] consentDurationTableColumnWidth = new float[]{240, 240};
        int[] consentDurationTableColumnAlignment = new int[]{HexPDF.LEFT, HexPDF.LEFT};
        document.drawTable(title,
                consentDurationTableColumnWidth,
                consentDurationTableColumnAlignment,
                HexPDF.LEFT);
    }

    @Override
    public void addConsentSigningDetails(HexPDF document, PatientDto patient, Boolean signedByPatient) throws IOException {
        if (signedByPatient) {
            // Consent is signed by Patient
            addPatientSigningDetails(document, patient);
        }
    }

    private void addPatientSigningDetails(HexPDF document, PatientDto patient) throws IOException {
        Date date = new Date();
        Object[][] signedDetails = {
                {createSignatureContent(patient, date)}
        };
        float[] patientDetailsColumnWidth = new float[]{480};
        int[] patientDetailsColumnAlignment = new int[]{HexPDF.LEFT};

        document.drawTable(signedDetails,
                patientDetailsColumnWidth,
                patientDetailsColumnAlignment,
                HexPDF.LEFT);
    }

    private String createSignatureContent(PatientDto patient, Date signedOnDateTime) {
        String patientFirstName = patient.getName().stream().map(name -> name.getFirstName()).findAny().orElse(null);
        String patientLastName = patient.getName().stream().map(name -> name.getLastName()).findAny().orElse(null);

        //String patientLastName = Optional.ofNullable(patient.getName().get(0).getLastName()).orElse("");
        //String patientFirstName = Optional.ofNullable(patient.getName().get(0).getFirstName()).orElse("");

        String patientFullName = patientFirstName.concat(SPACE_PATTERN + patientLastName);

        String email = patient.getTelecoms().stream()
                .filter(telecomDto -> telecomDto.getSystem().isPresent())
                .filter(telecomDto -> telecomDto.getSystem().get().equalsIgnoreCase(TELECOM_EMAIL))
                .map(telecomDto -> telecomDto.getValue().get())
                .findAny()
                .orElseThrow(NoDataFoundException::new);

        LocalDate signedDate = signedOnDateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        final String signedByContent = NEWLINE_CHARACTER + "Signed by: ".concat(patientFullName);
        final String signedByEmail = "Email: ".concat(email);
        final String signedOn = "Signed on: ".concat(formatLocalDate(signedDate, DATE_FORMAT_PATTERN));

        return signedByContent.concat(NEWLINE_CHARACTER).concat(signedByEmail).concat(NEWLINE_CHARACTER).concat(signedOn).concat(NEWLINE_CHARACTER);
    }

    private  String formatLocalDate(LocalDate localDate, String formatPattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        return localDate.format(formatter);
    }

    private String composeAddress(AddressDto addressDto) {
        return addressDto.getLine1()
                .concat(filterNullAddressValue(addressDto.getLine2()))
                .concat(filterNullAddressValue(addressDto.getCity()))
                .concat(filterNullAddressValue(addressDto.getStateCode()))
                .concat(filterNullAddressValue(addressDto.getPostalCode()))
                .concat(filterNullAddressValue(addressDto.getCountryCode()));
    }

    private static String filterNullAddressValue(String value) {
        final String commaPattern = ", ";
        if (value == null) {
            return "";
        } else {
            return commaPattern.concat(value);
        }
    }


}
