package gov.samhsa.c2s.c2ssofapi.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientDto {
    private String id;

    private String resourceURL;

    @Valid
    private List<IdentifierDto> identifier;

    private Optional<String> mrn;

    private boolean active;

    // Human Name (family, given name)
    private List<NameDto> name;

    @NotEmpty
    private String genderCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private LocalDate birthDate;

    private String race;

    private String ethnicity;

    private String birthSex;

    private List<AddressDto> addresses;

    private List<TelecomDto> telecoms;

    private String language;

    private Optional<List<FlagDto>> flags;

    Optional<String> organizationId;

    Optional<String> practitionerId;

}
