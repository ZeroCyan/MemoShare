package be.pbin.writeserver.data.payload.validation;

import be.pbin.writeserver.data.payload.Payload;

/**
 * Base interface for a chain of responsibility design pattern implementation.
 */
public interface PayloadValidator {
    ValidationResult validate(Payload payload);
}
