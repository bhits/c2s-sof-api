package gov.samhsa.c2s.c2ssofapi.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PractitionerDto {
    private String logicalId;

    private List<IdentifierDto> identifiers;

    private boolean active;

    private List<NameDto> name;

    private List<TelecomDto> telecoms;

    private List<AddressDto> addresses;
}
