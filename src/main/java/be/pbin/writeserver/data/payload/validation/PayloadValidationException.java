package be.pbin.writeserver.data.payload.validation;

import be.pbin.writeserver.data.DataProcessingException;

/**
 * This exception is raised by any of the {@link PayloadValidator} implementations to indicate encountering an error during payload validation.
 */
public class PayloadValidationException extends DataProcessingException {

    public PayloadValidationException(String message) {
        super(message);
    }
}
