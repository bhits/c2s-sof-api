package gov.samhsa.c2s.c2ssofapi.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import gov.samhsa.c2s.c2ssofapi.service.exception.PreconditionFailedException;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Configuration
public class FhirServiceConfig {

    public static final String HTTP_HEADER_FHIR_SERVER = "FhirServer";
    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String EMPTY_STRING = "";
    private final ConfigProperties configProperties;

    @Autowired
    public FhirServiceConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Bean
    public FhirContext fhirContext() {
        FhirContext fhirContext = FhirContext.forDstu3();
        fhirContext.getRestfulClientFactory().setSocketTimeout(Integer.parseInt(configProperties.getFhir().getClientSocketTimeoutInMs()));
        return fhirContext;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public IGenericClient fhirClient() {
        final Optional<HttpServletRequest> httpServletRequest = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
        final String fhirServerUrl = httpServletRequest
                .map(req -> req.getHeader(HTTP_HEADER_FHIR_SERVER))
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new PreconditionFailedException("'FhirServer' header is not available in the request"));
        final String accessToken = httpServletRequest
                .map(req -> req.getHeader(HTTP_HEADER_AUTHORIZATION))
                .filter(auth -> auth.startsWith(BEARER_PREFIX))
                .map(auth -> auth.replace(BEARER_PREFIX, EMPTY_STRING))
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new PreconditionFailedException("'Authorization' header is not avaiable in the request or it does not start with 'Bearer ' "));
        final IGenericClient fhirClient = fhirContext().newRestfulGenericClient(fhirServerUrl);
        fhirClient.registerInterceptor(new BearerTokenAuthInterceptor(accessToken));
        return fhirClient;
    }

    @Bean
    public IParser fhirJsonParser() {
        return fhirContext().newJsonParser();
    }

    @Bean
    public FhirValidator fhirValidator() {
        FhirValidator validator = fhirContext().newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);
        ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport());
        instanceValidator.setValidationSupport(support);
        return validator;
    }
}
