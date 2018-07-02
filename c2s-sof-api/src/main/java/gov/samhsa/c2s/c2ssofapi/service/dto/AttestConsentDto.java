package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AttestConsentDto {

    @NotNull
    private String signatureDataURL;
}
