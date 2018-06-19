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
public class GeneralConsentRelatedFieldDto {
    private List<ReferenceDto> fromActors;

    private List<ReferenceDto> toActors;

    private ValueSetDto purposeOfUse;

    private List<ValueSetDto> medicalInformation;
}
