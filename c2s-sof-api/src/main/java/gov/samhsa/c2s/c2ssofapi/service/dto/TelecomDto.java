package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelecomDto {

    @NotBlank
    private Optional<String> system;

    @NotBlank
    private Optional<String> value;

    private Optional<String> use;
}
