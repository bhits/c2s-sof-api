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

    @Autowired
    LookUpService lookUpService;

    @GetMapping()
    public LookUpDataDto getAllLookUpValues(@RequestParam(value = "lookUpTypeList", required = false) List<String> lookUpTypeList) {
        LookUpDataDto lookUpData = new LookUpDataDto();

        //Consent State Codes
        if (lookUpTypeList == null || lookUpTypeList.size() == 0 || lookUpTypeList.stream().anyMatch(LookUpTypeEnum.CONSENT_STATE_CODES.name()::equalsIgnoreCase)) {
            lookUpData.setConsentStateCodes(lookUpService.getConsentStateCodes());
        }

        //Purpose of use
        if (lookUpTypeList == null || lookUpTypeList.size() == 0 || lookUpTypeList.stream().anyMatch(LookUpTypeEnum.PURPOSE_OF_USE.name()::equalsIgnoreCase)) {
            lookUpData.setPurposeOfUse(lookUpService.getPurposeOfUse());
        }

        //Security Label
        if (lookUpTypeList == null || lookUpTypeList.size() == 0 || lookUpTypeList.stream().anyMatch(LookUpTypeEnum.SECURITY_LABEL.name()::equalsIgnoreCase)) {
            lookUpData.setSecurityLabel(lookUpService.getSecurityLabel());
        }
        return lookUpData;
    }
}
