package be.pbin.writeserver.data.payload.validation;

/**
 * This exception is raised by any of the {@link PayloadValidator} implementations to indicate encountering an error during payload validation.
 */
public class InvalidPayloadException extends Exception {

    public InvalidPayloadException(String message) {
        super(message);
    }
}
