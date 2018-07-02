package gov.samhsa.c2s.c2ssofapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoEmailFoundException extends RuntimeException{
    public NoEmailFoundException() {
    }

    public NoEmailFoundException(String message) {
        super(message);
    }

    public NoEmailFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoEmailFoundException(Throwable cause) {
        super(cause);
    }

    public NoEmailFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
