package gov.samhsa.c2s.c2ssofapi.domain;

import gov.samhsa.c2s.c2ssofapi.service.exception.USStateNotFoundException;

import java.util.Arrays;
import java.util.stream.Stream;

public enum USStateEnum {
    AL("AL", "ALABAMA"),
    AK("AK", "ALASKA"),
    AZ("AZ", "ARIZONA"),
    AR("AR", "ARKANSAS"),
    CA("CA", "CALIFORNIA"),
    CO("CO", "COLORADO"),
    CT("CT", "CONNECTICUT"),
    DC("DE", "WASHINGTON DC"),
    DE("DE", "DELAWARE"),
    FL("FL", "FLORIDA"),
    GA("GA", "GEORGIA"),
    HI("HI", "HAWAII"),
    ID("ID", "IDAHO"),
    IL("IL", "ILLINOIS"),
    IN("IN", "INDIANA"),
    IA("IA", "IOWA"),
    KS("KS", "KANSAS"),
    KY("KY", "KENTUCKY"),
    LA("LA", "LOUISIANA"),
    ME("ME", "MAINE"),
    MD("MD", "MARYLAND"),
    MA("MA", "MASSACHUSETTS"),
    MI("MI", "MICHIGAN"),
    MN("MN", "MINNESOTA"),
    MS("MS", "MISSISSIPPI"),
    MO("MO", "MISSOURI"),
    MT("MT", "MONTANA"),
    NE("NE", "NEBRASKA"),
    NV("NV", "NEVADA"),
    NH("NH", "NEW HAMPSHIRE"),
    NJ("NJ", "NEW JERSEY"),
    NM("NM", "NEW MEXICO"),
    NY("NY", "NEW YORK"),
    NC("NC", "NORTH CAROLINA"),
    ND("ND", "NORTH DAKOTA"),
    OH("OH", "OHIO"),
    OK("OK", "OKLAHOMA"),
    OR("OR", "OREGON"),
    PA("PA", "PENNSYLVANIA"),
    RI("RI", "RHODE ISLAND"),
    SC("SC", "SOUTH CAROLINA"),
    SD("SD", "SOUTH DAKOTA"),
    TN("TN", "TENNESSEE"),
    TX("TX", "TEXAS"),
    UT("UT", "UTAH"),
    VT("VT", "VERMONT"),
    VA("VA", "VIRGINIA"),
    WA("WA", "WASHINGTON"),
    WV("WV", "WEST VIRGINIA"),
    WI("WI", "WISCONSIN"),
    WY("WY", "WYOMING");

    private final String code;
    private final String name;

    USStateEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static USStateEnum fromCode(String code) {
        return asStream()
                .filter(state -> state.getCode().equals(code))
                .findAny()
                .orElseThrow(() -> new USStateNotFoundException("US State cannot be found with code: " + code));
    }

    public static Stream<USStateEnum> asStream() {
        return Arrays.stream(values());
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
