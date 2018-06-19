package gov.samhsa.c2s.c2ssofapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FHIRFormatErrorException extends RuntimeException {
    public FHIRFormatErrorException() { }
    public FHIRFormatErrorException(String message) {
        super(message);
    }

}
