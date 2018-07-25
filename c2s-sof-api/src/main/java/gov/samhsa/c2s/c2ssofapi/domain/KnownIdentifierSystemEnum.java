package gov.samhsa.c2s.c2ssofapi.domain;

import gov.samhsa.c2s.c2ssofapi.service.exception.DriverLicenseSystemByStateNotFoundException;
import gov.samhsa.c2s.c2ssofapi.service.exception.IdentifierSystemNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum KnownIdentifierSystemEnum {
    SSN("http://hl7.org/fhir/sid/us-ssn", "2.16.840.1.113883.4.1", "SSN", IdentifierTypeEnum.SB, Optional.empty()),
    MEDICARE_NUMBER("http://hl7.org/fhir/sid/us-medicare", "2.16.840.1.113883.4.572", "Medicare Number", IdentifierTypeEnum.SB, Optional.empty()),
    TAX_ID_INDIVIDUAL("urn:oid:2.16.840.1.113883.4.2", "2.16.840.1.113883.4.2", "Individual Tax ID", IdentifierTypeEnum.TAX, Optional.empty()),
    PASSPORT("urn:oid:2.16.840.1.113883.4.330", "2.16.840.1.113883.4.330", "Passport Number", IdentifierTypeEnum.PPN, Optional.empty()),
    TAX_ID_ORGANIZATION("urn:oid:2.16.840.1.113883.4.4", "2.16.840.1.113883.4.4", "Organization Tax ID", IdentifierTypeEnum.TAX, Optional.empty()),
    NPI("http://hl7.org/fhir/sid/us-npi", "2.16.840.1.113883.4.6", "NPI", IdentifierTypeEnum.PRN, Optional.empty()),
    DL("urn:oid:2.16.840.1.113883.4.3", "2.16.840.1.113883.4.3", "United States Driver License Number", IdentifierTypeEnum.DL, Optional.empty()),
    DL_ALASKA("urn:oid:2.16.840.1.113883.4.3.2", "2.16.840.1.113883.4.3.2", "Alaska Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.AK)),
    DL_ALABAMA("urn:oid:2.16.840.1.113883.4.3.1", "2.16.840.1.113883.4.3.1", "Alabama Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.AL)),
    DL_ARKANSAS("urn:oid:2.16.840.1.113883.4.3.5", "2.16.840.1.113883.4.3.5", "Arkansas Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.AR)),
    DL_ARIZONA("urn:oid:2.16.840.1.113883.4.3.4", "2.16.840.1.113883.4.3.4", "Arizona Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.AZ)),
    DL_CALIFORNIA("urn:oid:2.16.840.1.113883.4.3.6", "2.16.840.1.113883.4.3.6", "California Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.CA)),
    DL_COLORADO("urn:oid:2.16.840.1.113883.4.3.8", "2.16.840.1.113883.4.3.8", "Colorado Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.CO)),
    DL_CONNECTICUT("urn:oid:2.16.840.1.113883.4.3.9", "2.16.840.1.113883.4.3.9", "Connecticut Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.CT)),
    DL_DC("urn:oid:2.16.840.1.113883.4.3.11", "2.16.840.1.113883.4.3.11", "DC Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.DC)),
    DL_DELAWARE("urn:oid:2.16.840.1.113883.4.3.10", "2.16.840.1.113883.4.3.10", "Delaware Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.DE)),
    DL_FLORIDA("urn:oid:2.16.840.1.113883.4.3.12", "2.16.840.1.113883.4.3.12", "Florida Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.FL)),
    DL_GEORGIA("urn:oid:2.16.840.1.113883.4.3.13", "2.16.840.1.113883.4.3.13", "Georgia Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.GA)),
    DL_HAWAII("urn:oid:2.16.840.1.113883.4.3.15", "2.16.840.1.113883.4.3.15", "Hawaii Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.HI)),
    DL_INDIANA("urn:oid:2.16.840.1.113883.4.3.18", "2.16.840.1.113883.4.3.18", "Indiana Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.IN)),
    DL_IOWA("urn:oid:2.16.840.1.113883.4.3.19", "2.16.840.1.113883.4.3.19", "Iowa Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.IA)),
    DL_IDAHO("urn:oid:2.16.840.1.113883.4.3.16", "2.16.840.1.113883.4.3.16", "Idaho Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.ID)),
    DL_ILLINOIS("urn:oid:2.16.840.1.113883.4.3.17", "2.16.840.1.113883.4.3.17", "Illinois Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.IL)),
    DL_KANSAS("urn:oid:2.16.840.1.113883.4.3.20", "2.16.840.1.113883.4.3.20", "Kansas Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.KS)),
    DL_KENTUCKY("urn:oid:2.16.840.1.113883.4.3.21", "2.16.840.1.113883.4.3.21", "Kentucky Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.KY)),
    DL_LOUISIANA("urn:oid:2.16.840.1.113883.4.3.22", "2.16.840.1.113883.4.3.22", "Louisiana Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.LA)),
    DL_MASSACHUSETTS("urn:oid:2.16.840.1.113883.4.3.25", "2.16.840.1.113883.4.3.25", "Massachusetts Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.MA)),
    DL_MARYLAND("urn:oid:2.16.840.1.113883.4.3.24", "2.16.840.1.113883.4.3.24", "Maryland Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.MD)),
    DL_MAINE("urn:oid:2.16.840.1.113883.4.3.23", "2.16.840.1.113883.4.3.23", "Maine Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.ME)),
    DL_MICHIGAN("urn:oid:2.16.840.1.113883.4.3.26", "2.16.840.1.113883.4.3.26", "Michigan Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.MI)),
    DL_MINNESOTA("urn:oid:2.16.840.1.113883.4.3.27", "2.16.840.1.113883.4.3.27", "Minnesota Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.MN)),
    DL_MISSOURI("urn:oid:2.16.840.1.113883.4.3.29", "2.16.840.1.113883.4.3.29", "Missouri Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.MO)),
    DL_MISSISSIPPI("urn:oid:2.16.840.1.113883.4.3.28", "2.16.840.1.113883.4.3.28", "Mississippi Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.MS)),
    DL_MONTANA("urn:oid:2.16.840.1.113883.4.3.30", "2.16.840.1.113883.4.3.30", "Montana Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.MT)),
    DL_NEW_YORK("urn:oid:2.16.840.1.113883.4.3.36", "2.16.840.1.113883.4.3.36", "New York Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.NY)),
    DL_NORTH_CAROLINA("urn:oid:2.16.840.1.113883.4.3.37", "2.16.840.1.113883.4.3.37", "North Carolina Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.NC)),
    DL_NORTH_DAKOTA("urn:oid:2.16.840.1.113883.4.3.38", "2.16.840.1.113883.4.3.38", "North Dakota Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.ND)),
    DL_NEBRASKA("urn:oid:2.16.840.1.113883.4.3.31", "2.16.840.1.113883.4.3.31", "Nebraska Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.NE)),
    DL_NEW_HAMPSHIRE("urn:oid:2.16.840.1.113883.4.3.33", "2.16.840.1.113883.4.3.33", "New Hampshire Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.NH)),
    DL_NEW_JERSEY("urn:oid:2.16.840.1.113883.4.3.34", "2.16.840.1.113883.4.3.34", "New Jersey Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.NJ)),
    DL_NEW_MEXICO("urn:oid:2.16.840.1.113883.4.3.35", "2.16.840.1.113883.4.3.35", "New Mexico Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.NM)),
    DL_NEVADA("urn:oid:2.16.840.1.113883.4.3.32", "2.16.840.1.113883.4.3.32", "Nevada Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.NV)),
    DL_OHIO("urn:oid:2.16.840.1.113883.4.3.39", "2.16.840.1.113883.4.3.39", "Ohio Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.OH)),
    DL_OKLAHOMA("urn:oid:2.16.840.1.113883.4.3.40", "2.16.840.1.113883.4.3.40", "Oklahoma Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.OK)),
    DL_OREGON("urn:oid:2.16.840.1.113883.4.3.41", "2.16.840.1.113883.4.3.41", "Oregon Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.OR)),
    DL_PENNSYLVANIA("urn:oid:2.16.840.1.113883.4.3.42", "2.16.840.1.113883.4.3.42", "Pennsylvania Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.PA)),
    DL_RHODE_ISLAND("urn:oid:2.16.840.1.113883.4.3.44", "2.16.840.1.113883.4.3.44", "Rhode Island Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.RI)),
    DL_SOUTH_CAROLINA("urn:oid:2.16.840.1.113883.4.3.45", "2.16.840.1.113883.4.3.45", "South Carolina Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.SC)),
    DL_SOUTH_DAKOTA("urn:oid:2.16.840.1.113883.4.3.46", "2.16.840.1.113883.4.3.46", "South Dakota Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.SD)),
    DL_TENNESSEE("urn:oid:2.16.840.1.113883.4.3.47", "2.16.840.1.113883.4.3.47", "Tennessee Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.TN)),
    DL_TEXAS("urn:oid:2.16.840.1.113883.4.3.48", "2.16.840.1.113883.4.3.48", "Texas Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.TX)),
    DL_UTAH("urn:oid:2.16.840.1.113883.4.3.49", "2.16.840.1.113883.4.3.49", "Utah Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.UT)),
    DL_VIRGINIA("urn:oid:2.16.840.1.113883.4.3.51", "2.16.840.1.113883.4.3.51", "Virginia Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.VA)),
    DL_VERMONT("urn:oid:2.16.840.1.113883.4.3.50", "2.16.840.1.113883.4.3.50", "Vermont Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.VT)),
    DL_WASHINGTON("urn:oid:2.16.840.1.113883.4.3.53", "2.16.840.1.113883.4.3.53", "Washington Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.WA)),
    DL_WISCONSIN("urn:oid:2.16.840.1.113883.4.3.55", "2.16.840.1.113883.4.3.55", "Wisconsin Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.WI)),
    DL_WEST_VIRGINIA("urn:oid:2.16.840.1.113883.4.3.54", "2.16.840.1.113883.4.3.54", "West Virginia Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.WV)),
    DL_WYOMING("urn:oid:2.16.840.1.113883.4.3.56", "2.16.840.1.113883.4.3.56", "Wyoming Driver's License", IdentifierTypeEnum.DL, Optional.of(USStateEnum.WY));

    private final String uri;
    private final String oid;
    private final String display;
    private final IdentifierTypeEnum type;
    private final Optional<USStateEnum> usStateOptional;

    KnownIdentifierSystemEnum(String uri, String oid, String display, IdentifierTypeEnum type, Optional<USStateEnum> usStateOptional) {
        this.uri = uri;
        this.oid = oid;
        this.display = display;
        this.type = type;
        this.usStateOptional = usStateOptional;
    }

    public static Stream<KnownIdentifierSystemEnum> asStream() {
        return Arrays.stream(values());
    }

    public static KnownIdentifierSystemEnum fromUri(String uri) {
        return fromUriAsOptional(uri)
                .orElseThrow(() -> new IdentifierSystemNotFoundException("Cannot find identifier system with uri: " + uri));
    }

    public static Optional<KnownIdentifierSystemEnum> fromUriAsOptional(String uri) {
        return asStream()
                .filter(kis -> uri.equals(kis.getUri()) || uri.equals("urn:oid:" + kis.getOid()))
                .findAny();
    }

    public static KnownIdentifierSystemEnum fromOid(String oid) {
        return fromOidAsOptional(oid)
                .orElseThrow(() -> new IdentifierSystemNotFoundException("Cannot find identifier system with oid: " + oid));
    }

    public static Optional<KnownIdentifierSystemEnum> fromOidAsOptional(String oid) {
        return asStream()
                .filter(kis -> kis.getOid().equals(oid))
                .findAny();
    }

    public static Stream<KnownIdentifierSystemEnum> driverLicenseSystemsAsStream() {
        return asStream()
                .filter(kis -> IdentifierTypeEnum.DL.equals(kis.getType()));
    }

    public static List<KnownIdentifierSystemEnum> driverLicenseSystemsAsList() {
        return asStream()
                .filter(kis -> IdentifierTypeEnum.DL.equals(kis.getType()))
                .collect(toList());
    }

    public static KnownIdentifierSystemEnum driverLicenseSystemByState(USStateEnum state) {
        return driverLicenseSystemsAsStream()
                .filter(kis -> kis.usStateOptional
                        .filter(state::equals).isPresent())
                .findAny()
                .orElseThrow(() -> new DriverLicenseSystemByStateNotFoundException("Cannot find identifier system with state: " + state));
    }

    public static Stream<KnownIdentifierSystemEnum> identifierSystemsByIdentifierTypeEnumAsStream(IdentifierTypeEnum IdentifierTypeEnum) {
        return asStream()
                .filter(identifierSystem -> identifierSystem.getType().equals(IdentifierTypeEnum));
    }

    public static List<KnownIdentifierSystemEnum> identifierSystemsByIdentifierTypeEnum(IdentifierTypeEnum IdentifierTypeEnum) {
        return identifierSystemsByIdentifierTypeEnumAsStream(IdentifierTypeEnum)
                .collect(toList());
    }

    public String getUri() {
        return uri;
    }

    public String getOid() {
        return oid;
    }

    public String getDisplay() {
        return display;
    }

    public IdentifierTypeEnum getType() {
        return type;
    }
}
