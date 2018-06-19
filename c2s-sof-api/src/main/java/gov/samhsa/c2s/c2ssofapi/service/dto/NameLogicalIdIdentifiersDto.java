package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

@Data
public class NameLogicalIdIdentifiersDto {
    @NotBlank
    private String name;
    private String logicalId;
    private String resourceURL;
    private List<IdentifierDto> identifiers;
}
