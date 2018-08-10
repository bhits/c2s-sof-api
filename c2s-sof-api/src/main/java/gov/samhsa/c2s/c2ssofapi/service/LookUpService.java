package gov.samhsa.c2s.c2ssofapi.service;

import gov.samhsa.c2s.c2ssofapi.service.dto.ValueSetDto;

import java.util.List;

public interface LookUpService {
    List<ValueSetDto> getConsentStateCodes();
    List<ValueSetDto> getConsentPurposeOfUse();
    List<ValueSetDto> getConsentSecurityLabel();
    List<ValueSetDto> getConsentSecurityRole();
    List<ValueSetDto> getConsentAction();
}
