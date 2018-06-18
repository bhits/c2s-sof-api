package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {

    private String line1;
    private String line2;
    private String city;

    @Pattern(regexp = "\\d{5}(?:[-\\s]\\d{4})?")
    private String postalCode;

    private String stateCode;
    private String countryCode;
    private String use;
}
