package gov.samhsa.c2s.c2ssofapi.domain;


import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;

import java.util.Arrays;
import java.util.stream.Stream;

public enum LanguageEnum {
    eng("eng", "English"),
    spa("spa", "Spanish; Castilian"),
    hin("hin", "Hindi");

    private final String code;
    private final String name;

    LanguageEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LanguageEnum fromCode(String code) {
        return asStream()
                .filter(language -> language.getCode().equals(code))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("Language cannot be found with code: " + code));
    }

    public static Stream<LanguageEnum> asStream() {
        return Arrays.stream(values());
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
