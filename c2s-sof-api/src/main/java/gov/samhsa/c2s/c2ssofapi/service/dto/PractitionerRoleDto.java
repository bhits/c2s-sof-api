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
public class PractitionerRoleDto {

    private String logicalId;

    private boolean active;

    private ReferenceDto organization;

    private ReferenceDto practitioner;

    private List<ValueSetDto> specialty;

    private List<ValueSetDto> code;
}
