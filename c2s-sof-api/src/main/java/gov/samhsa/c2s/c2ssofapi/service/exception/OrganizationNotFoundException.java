package gov.samhsa.c2s.c2ssofapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class OrganizationNotFoundException extends RuntimeException {
    public OrganizationNotFoundException() {
        super();
    }

    public OrganizationNotFoundException(String message) {
        super(message);
    }
}
