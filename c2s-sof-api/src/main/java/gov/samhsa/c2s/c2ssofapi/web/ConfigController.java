package gov.samhsa.c2s.c2ssofapi.web;

import gov.samhsa.c2s.c2ssofapi.config.UiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {
    @Autowired
    private UiConfigProperties uiConfigProperties;

    @GetMapping("/config")
    public UiConfigProperties getConfig() {
        return uiConfigProperties;
    }
}
