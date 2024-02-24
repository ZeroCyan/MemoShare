package be.pbin.writeserver.data.payload.validation.validators;

import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.validation.PayloadValidator;
import be.pbin.writeserver.data.payload.validation.ValidationResult;
import be.pbin.writeserver.data.payload.validation.ValidationStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class DummyValidator implements PayloadValidator {

    @Override
    public ValidationResult validate(Payload payload) {
        if (payload.payload().startsWith("DUMMY")){ //todo: remove. Function: to trigger a validation error from a request.
            return new ValidationResult(ValidationStatus.INVALID, List.of("DummyValidator: DUMMY triggered."));
        }
        return new ValidationResult(ValidationStatus.VALIDATED, new ArrayList<>());
    }
}
