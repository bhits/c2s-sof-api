package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RevokeConsentDto {

    @NotNull
    private String signatureDataURL;
}
