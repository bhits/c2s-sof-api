package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AbstractCareTeamDto {
    public enum CareTeamType{
        PRACTITIONER, ORGANIZATION, RELATEDPERSON;
    }
    private String id;

    private String display;

    @Valid
    @NotEmpty
    private List<IdentifierDto> identifiers;

    private Optional<String> phoneNumber;

    private Optional<String> email;

    @Valid
    private AddressDto address;

    private CareTeamType careTeamType;
}
