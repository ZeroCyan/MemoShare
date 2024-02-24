package be.pbin.writeserver.service.implementations;

import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.validation.InvalidPayloadException;
import be.pbin.writeserver.data.payload.validation.PayloadValidator;
import be.pbin.writeserver.data.payload.validation.ValidationResult;
import be.pbin.writeserver.data.payload.validation.ValidationStatus;
import be.pbin.writeserver.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final List<PayloadValidator> validationChain;

    public ValidationServiceImpl(List<PayloadValidator> validationChain) {
        this.validationChain = validationChain;
    }

    //todo: the unintuitiveness of testing this class suggests there is a cleaner way.
    @Override
    public void validate(Payload payload) throws InvalidPayloadException {
        ValidationResult validationResult = this.validatePayload(payload);

        if (validationResult.validationStatus() == ValidationStatus.INVALID) {
            String errorMessage = "Payload validation errors encountered: " + String.join(" :: ", validationResult.errors());
            logger.warn(errorMessage);
            throw new InvalidPayloadException(errorMessage);
        }
    }

    private ValidationResult validatePayload(Payload payload) {
        List<String> errors = new ArrayList<>();

        for (PayloadValidator validator : validationChain) {
            ValidationResult result = validator.validate(payload);
            if (result.validationStatus() == ValidationStatus.INVALID) {
                errors.addAll(result.errors());
            }
        }
        return new ValidationResult(errors.isEmpty() ? ValidationStatus.VALIDATED : ValidationStatus.INVALID, errors);
    }
}
