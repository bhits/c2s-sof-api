package gov.samhsa.c2s.c2ssofapi.service.exception;

public class IdentifierSystemNotFoundException extends RuntimeException {
    public IdentifierSystemNotFoundException() {
    }

    public IdentifierSystemNotFoundException(String message) {
        super(message);
    }

    public IdentifierSystemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentifierSystemNotFoundException(Throwable cause) {
        super(cause);
    }

    public IdentifierSystemNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
