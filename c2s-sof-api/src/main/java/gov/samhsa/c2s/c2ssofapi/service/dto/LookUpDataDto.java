package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LookUpDataDto {
    List<ValueSetDto> consentStateCodes;
    List<ValueSetDto> securityLabel;
    List<ValueSetDto> purposeOfUse;
    List<ValueSetDto> consentSecurityRole;
    List<ValueSetDto> consentAction;
}
