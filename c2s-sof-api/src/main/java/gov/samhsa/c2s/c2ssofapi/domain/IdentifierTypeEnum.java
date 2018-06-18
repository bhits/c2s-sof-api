package gov.samhsa.c2s.c2ssofapi.domain;

import java.util.Arrays;
import java.util.stream.Stream;

public enum IdentifierTypeEnum {
    //http://hl7.org/fhir/v2/0203
    DL("DL", "Driver's license number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    PPN("PPN", "Passport number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    BRN("BRN", "Breed Registry Number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    MR("MR", "Medical record number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    MCN("MCN", "Microchip Number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    EN("EN", "Employer number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    TAX("TAX", "Tax ID number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    NIIP("NIIP", "National Insurance Payor Identifier (Payor)", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    PRN("PRN", "Provider number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    MD("MD", "Medical License number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    DR("DR", "Donor Registration Number", Constants.HTTP_HL7_ORG_FHIR_V2_0203),
    ACSN("ACSN", "Accession ID", Constants.HTTP_HL7_ORG_FHIR_V2_0203),

    //http://hl7.org/fhir/identifier-type
    UDI("UDI", "Universal Device Identifier", Constants.HTTP_HL7_ORG_FHIR_IDENTIFIER_TYPE),
    SNO("SNO", "Serial Number", Constants.HTTP_HL7_ORG_FHIR_IDENTIFIER_TYPE),
    SB("SB", "Social Beneficiary Identifier", Constants.HTTP_HL7_ORG_FHIR_IDENTIFIER_TYPE),
    PLAC("PLAC", "Placer Identifier", Constants.HTTP_HL7_ORG_FHIR_IDENTIFIER_TYPE),
    FILL("FILL", "Filler Identifier", Constants.HTTP_HL7_ORG_FHIR_IDENTIFIER_TYPE);

    private final String code;
    private final String display;
    private final String system;

    IdentifierTypeEnum(String code, String display, String system) {
        this.code = code;
        this.display = display;
        this.system = system;
    }

    public static Stream<IdentifierTypeEnum> asStream() {
        return Arrays.stream(values());
    }

    private static class Constants {
        public static final String HTTP_HL7_ORG_FHIR_V2_0203 = "http://hl7.org/fhir/v2/0203";
        public static final String HTTP_HL7_ORG_FHIR_IDENTIFIER_TYPE = "http://hl7.org/fhir/identifier-type";
    }
}
