package gov.samhsa.c2s.c2ssofapi.service;

import gov.samhsa.c2s.c2ssofapi.service.dto.AbstractCareTeamDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.ConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.GeneralConsentRelatedFieldDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PdfDto;

import java.util.List;
import java.util.Optional;

public interface ConsentService {

    PageDto<DetailedConsentDto> getConsents(Optional<String> patient, Optional<String> practitioner, Optional<String> status, Optional<Boolean> generalDesignation, Optional<Integer> pageNumber, Optional<Integer> pageSize);

    void createConsent(ConsentDto consentDto);

    void updateConsent(String consentId, ConsentDto consentDto);

    DetailedConsentDto getConsentsById(String consentId);

    GeneralConsentRelatedFieldDto getGeneralConsentRelatedFields(String patient);

    PdfDto createConsentPdf(String consentId);

    void attestConsent(String consentId);

    void revokeConsent(String consentId);

    PageDto<AbstractCareTeamDto> getActors(Optional<String> patientId, Optional<String> name, Optional<String> actorType, Optional<List<String>> actorsAlreadyAssigned, Optional<Integer> pageNumber, Optional<Integer> pageSize);
}
