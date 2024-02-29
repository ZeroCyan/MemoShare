package be.pbin.webserver.validation;

/**
 * Base interface for a chain of responsibility design pattern implementation.
 */
public interface PayloadValidator {
    void validate(String payload) throws PayloadValidationException;
}
