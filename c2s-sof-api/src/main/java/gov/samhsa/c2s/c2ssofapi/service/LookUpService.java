package gov.samhsa.c2s.c2ssofapi.service;

import gov.samhsa.c2s.c2ssofapi.service.dto.ValueSetDto;

import java.util.List;

public interface LookUpService {
    List<ValueSetDto> getConsentStateCodes();

    List<ValueSetDto> getPurposeOfUse();

    List<ValueSetDto> getSecurityLabel();
    List<ValueSetDto>  getSecurityRole();
    List<ValueSetDto>  getConsentAction();
}
