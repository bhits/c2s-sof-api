package gov.samhsa.c2s.c2ssofapi.service.dto;

import java.util.Arrays;

public enum LookUpTypeEnum {
    CONSENT_STATE_CODES,
    PURPOSE_OF_USE,
    SECURITY_LABEL,
    CONSENT_SECURITY_ROLE,
    CONSENT_ACTION;

    public static boolean contains(String s) {
        return Arrays.stream(values()).anyMatch(key -> key.name().equalsIgnoreCase(s));
    }

}
