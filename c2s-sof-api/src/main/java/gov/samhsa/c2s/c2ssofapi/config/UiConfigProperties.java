package gov.samhsa.c2s.c2ssofapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "c2s-sof-ui-config")
@Data
public class UiConfigProperties {
    private String grantType;
    private String clientId;
    private String launchUri;
    private String redirectUri;
    private String scope;
}
