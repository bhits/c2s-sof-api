package gov.samhsa.c2s.c2ssofapi.service.dto;

public enum LookupPathUrls {
    CONSENT_STATE_CODE("consent state code", Constants.CONSENT_STATE_PATH),
    PURPOSE_OF_USE("consent purpose of use", Constants.PURPOSE_OF_USE_PATH),
    CONSENT_SECURITY_ROLE("security role", Constants.CONSENT_SECURITY_ROLE_PATH),
    CONSENT_ACTION("consent action", Constants.CONSENT_ACTION_PATH),
    SECURITY_LABEL("security label", Constants.SECURITY_LABEL_PATH);

    private final String type;
    private final String urlPath;

    LookupPathUrls(String type, String urlPath) {
        this.type = type;
        this.urlPath = urlPath;
    }

    public String getType() {
        return type;
    }

    public String getUrlPath() {
        return urlPath;
    }

    // Todo: Need to clean up unused/unnecessary constant variables and rename PROVIDER_ROLE_PATH to PRACTITIONER_ROLE_PATH and
    private static class Constants {
        static final String CONSENT_STATE_PATH = "/ValueSet/$expand?url=http://hl7.org/fhir/ValueSet/consent-state-codes";
        static final String PURPOSE_OF_USE_PATH = "/ValueSet/ocp-purposeofuse";
        static final String SECURITY_LABEL_PATH="/ValueSet/ocp-security-label";
        static final String CONSENT_SECURITY_ROLE_PATH = "/ValueSet/$expand?url=http://hl7.org/fhir/ValueSet/security-role-type";
        static final String CONSENT_ACTION_PATH = "/ValueSet/$expand?url=http://hl7.org/fhir/ValueSet/consent-action";
    }
}
