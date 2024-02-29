package be.pbin.webserver.validation;

public interface PayloadValidationService {

    boolean hasValidationErrors(String payload);
}
