package gov.samhsa.c2s.c2ssofapi.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NameDto {
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
}
