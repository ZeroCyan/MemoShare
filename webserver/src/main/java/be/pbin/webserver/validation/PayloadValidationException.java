package be.pbin.webserver.validation;


/**
 * This exception is raised by any of the {@link PayloadValidator} implementations to indicate encountering an error during payload validation.
 */
public class PayloadValidationException extends Exception {

    public PayloadValidationException(String message) {
        super(message);
    }
}
