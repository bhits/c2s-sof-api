package gov.samhsa.c2s.c2ssofapi.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "c2s-sof-api.pdf")
@Data
@Validated
public class PdfProperties {

    @NotNull
    @Valid
    public List<PdfConfig> pdfConfigs;

    @Data
    public static class PdfConfig {
        @NotBlank
        public String type;

        @NotBlank
        public String title;

    }
}
