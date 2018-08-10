package gov.samhsa.c2s.c2ssofapi.web;

import gov.samhsa.c2s.c2ssofapi.service.LookUpService;
import gov.samhsa.c2s.c2ssofapi.service.dto.LookUpDataDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.LookUpTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/lookups")
public class LookUpController {

    private final LookUpService lookUpService;

    @Autowired
    public LookUpController(LookUpService lookUpService) {
        this.lookUpService = lookUpService;
    }

    @GetMapping()
    public LookUpDataDto getAllLookUpValues(@RequestParam(value = "lookUpTypeList", required = false) List<String> lookUpTypeList) {
        LookUpDataDto lookUpData = new LookUpDataDto();

        // Consent State Codes
        if (lookUpTypeList == null || lookUpTypeList.size() == 0 || lookUpTypeList.stream().anyMatch(LookUpTypeEnum.CONSENT_STATE_CODES.name()::equalsIgnoreCase)) {
            log.info("Getting look up values for " + LookUpTypeEnum.CONSENT_STATE_CODES.name());
            lookUpData.setConsentStateCodes(lookUpService.getConsentStateCodes());
        }

        // Purpose of use
        if (lookUpTypeList == null || lookUpTypeList.size() == 0 || lookUpTypeList.stream().anyMatch(LookUpTypeEnum.PURPOSE_OF_USE.name()::equalsIgnoreCase)) {
            log.info("Getting look up values for " + LookUpTypeEnum.PURPOSE_OF_USE.name());
            lookUpData.setPurposeOfUse(lookUpService.getConsentPurposeOfUse());
        }

        // Security Label
        if (lookUpTypeList == null || lookUpTypeList.size() == 0 || lookUpTypeList.stream().anyMatch(LookUpTypeEnum.SECURITY_LABEL.name()::equalsIgnoreCase)) {
            log.info("Getting look up values for " + LookUpTypeEnum.SECURITY_LABEL.name());
            lookUpData.setSecurityLabel(lookUpService.getConsentSecurityLabel());
        }

        // Security Role
        if (lookUpTypeList == null || lookUpTypeList.size() == 0 || lookUpTypeList.stream().anyMatch(LookUpTypeEnum.CONSENT_SECURITY_ROLE.name()::equalsIgnoreCase)) {
            log.info("Getting look up values for " + LookUpTypeEnum.CONSENT_SECURITY_ROLE.name());
            lookUpData.setConsentSecurityRole(lookUpService.getConsentSecurityRole());
        }

        // Consent Action
        if (lookUpTypeList == null || lookUpTypeList.size() == 0 || lookUpTypeList.stream().anyMatch(LookUpTypeEnum.CONSENT_ACTION.name()::equalsIgnoreCase)) {
            log.info("Getting look up values for " + LookUpTypeEnum.CONSENT_ACTION.name());
            lookUpData.setConsentAction(lookUpService.getConsentAction());
        }
        return lookUpData;
    }
}
