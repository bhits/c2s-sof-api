package gov.samhsa.c2s.c2ssofapi.service.exception;

public class DriverLicenseSystemByStateNotFoundException extends IdentifierSystemNotFoundException {
    public DriverLicenseSystemByStateNotFoundException() {
    }

    public DriverLicenseSystemByStateNotFoundException(String message) {
        super(message);
    }

    public DriverLicenseSystemByStateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DriverLicenseSystemByStateNotFoundException(Throwable cause) {
        super(cause);
    }

    public DriverLicenseSystemByStateNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
