package gov.samhsa.c2s.c2ssofapi.constants;

public final class ConsentConstants {

    public static final String PSEUDO_ORGANIZATION_NAME = "Omnibus Care Plan (SAMHSA)";
    public static final String PSEUDO_ORGANIZATION_TAX_ID = "530196960";

    public static final String CONSENT_CUSTODIAN_CODE = "CST";
    public static final String CONSENT_INFORMANT_RECIPIENT_CODE = "IRCP";

    public static final String CONSENT_PURPOSE_OF_USE_CODING_SYSTEM = "http://hl7.org/fhir/v3/ActReason";

    public static final String CONSENT_ACTION_CODING_SYSTEM = "http://hl7.org/fhir/consentaction";
    public static final String CONSENT_ACTION_CODE = "disclose";
    public static final String CONSENT_ACTION_DISPLAY = "Disclose";

    public static final String CONTENT_TYPE = "application/pdf";
    public static final Boolean OPERATED_BY_PATIENT = true;

    // PRIVATE //
    private ConsentConstants(){
        throw new AssertionError();
    }
}
