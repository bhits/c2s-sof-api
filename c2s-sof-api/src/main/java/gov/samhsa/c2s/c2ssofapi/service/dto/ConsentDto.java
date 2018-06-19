package gov.samhsa.c2s.c2ssofapi.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentDto {

    private String logicalId;

    private IdentifierDto identifier;

    private PeriodDto period;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private LocalDate dateTime;

    private String status;

    private boolean generalDesignation;

    private ReferenceDto patient;

    private List<ReferenceDto> fromActor;

    private List<ReferenceDto> toActor;

    private List<ValueSetDto> category;

    private List<ValueSetDto> purpose;

    private List<ValueSetDto> medicalInformation;

    private ConsentMedicalInfoType consentMedicalInfoType;

    private byte[] sourceAttachment;
}
