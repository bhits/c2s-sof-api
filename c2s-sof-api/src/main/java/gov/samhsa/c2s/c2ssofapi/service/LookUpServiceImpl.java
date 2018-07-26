package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.samhsa.c2s.c2ssofapi.service.dto.LookupPathUrls;
import gov.samhsa.c2s.c2ssofapi.service.dto.ValueSetDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;
import gov.samhsa.c2s.c2ssofapi.util.LookUpUtil;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class LookUpServiceImpl implements LookUpService {

    private final IGenericClient fhirClient;

    public LookUpServiceImpl(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    @Override
    public List<ValueSetDto> getConsentStateCodes() {
        return getValueSetsByValueSetExpansion(LookupPathUrls.CONSENT_STATE_CODE.getUrlPath(), LookupPathUrls.CONSENT_STATE_CODE.getType());
    }

    @Override
    public List<ValueSetDto> getPurposeOfUse() {
        return getValueSetsByValueSetCompose(LookupPathUrls.PURPOSE_OF_USE.getUrlPath(), LookupPathUrls.PURPOSE_OF_USE.getType());
    }

    @Override
    public List<ValueSetDto> getSecurityLabel() {
        return getValueSetsByValueSetCompose(LookupPathUrls.SECURITY_LABEL.getUrlPath(), LookupPathUrls.SECURITY_LABEL.getType());
    }

    private ValueSet getValueSets(String urlPath, String type) {
        ValueSet response;
        String url = fhirClient.getServerBase() + urlPath;
        try {
            response = (ValueSet) fhirClient.search().byUrl(url).execute();
        } catch (ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException e) {
            log.error("Query was unsuccessful - Could not find any " + type + " code", e.getMessage());
            throw new ResourceNotFoundException("Query was unsuccessful - Could not find any " + type + " code", e);
        }
        return response;
    }

    private List<ValueSetDto> getValueSetsByValueSetExpansion(String urlPath, String type) {
        List<ValueSetDto> valueSets;
        ValueSet response = getValueSets(urlPath, type);
        List<ValueSet.ValueSetExpansionContainsComponent> valueSetList = response.getExpansion().getContains();
        valueSets = valueSetList.stream().map(LookUpUtil::convertExpansionComponentToValueSetDto).collect(Collectors.toList());
        log.info("Found " + valueSets.size() + type + ".");
        return valueSets;
    }

    private List<ValueSetDto> getValueSetsByValueSetCompose(String urlPath, String type) {
        List<ValueSetDto> valueSets;
        ValueSet response = getValueSets(urlPath, type);
        List<ValueSet.ConceptSetComponent> valueSetList = response.getCompose().getInclude();
        valueSets = valueSetList.stream().flatMap(obj -> obj.getConcept().stream()).map(LookUpUtil::convertConceptReferenceToValueSetDto).collect(Collectors.toList());
        log.info("Found " + valueSets.size() + type + ".");
        return valueSets;
    }

    @Override
    public List<ValueSetDto> getSecurityRole() {
        List<ValueSetDto> securityRoleList = new ArrayList<>();
        ValueSet response = getValueSets(LookupPathUrls.CONSENT_SECURITY_ROLE.getUrlPath(), LookupPathUrls.CONSENT_SECURITY_ROLE.getType());
        if (LookUpUtil.isValueSetAvailableInServer(response, LookupPathUrls.CONSENT_SECURITY_ROLE.getType())) {
            List<ValueSet.ValueSetExpansionContainsComponent> valueSetList = response.getExpansion().getContains();
            securityRoleList = valueSetList.stream().map(LookUpUtil::convertExpansionComponentToValueSetDto).collect(Collectors.toList());
        }
        log.info("Found " + securityRoleList.size() + " security role.");
        return securityRoleList;
    }

    @Override
    public List<ValueSetDto> getConsentAction() {
        List<ValueSetDto> consentActionList = new ArrayList<>();
        ValueSet response = getValueSets(LookupPathUrls.CONSENT_ACTION.getUrlPath(), LookupPathUrls.CONSENT_ACTION.getType());
        if (LookUpUtil.isValueSetAvailableInServer(response, LookupPathUrls.CONSENT_ACTION.getType())) {
            List<ValueSet.ValueSetExpansionContainsComponent> valueSetList = response.getExpansion().getContains();
            consentActionList = valueSetList.stream().map(LookUpUtil::convertExpansionComponentToValueSetDto).collect(Collectors.toList());
        }
        log.info("Found " + consentActionList.size() + " consent Action.");
        return consentActionList;
    }


}
