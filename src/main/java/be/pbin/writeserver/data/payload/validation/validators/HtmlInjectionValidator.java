package be.pbin.writeserver.data.payload.validation.validators;

import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.validation.PayloadValidator;
import be.pbin.writeserver.data.payload.validation.ValidationResult;
import be.pbin.writeserver.data.payload.validation.ValidationStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class HtmlInjectionValidator implements PayloadValidator {
    @Override
    public ValidationResult validate(Payload payload) {
        //todo
        return new ValidationResult(ValidationStatus.VALIDATED, new ArrayList<>());
    }
}
