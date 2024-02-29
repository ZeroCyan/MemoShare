package be.pbin.webserver.validation.validators;


import be.pbin.webserver.validation.PayloadValidationException;
import be.pbin.webserver.validation.PayloadValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class DummyValidator implements PayloadValidator {

    @Override
    public void validate(String payload) throws PayloadValidationException {
        if (payload.startsWith("DUMMY")) { //todo: Remove from production code. Function: to trigger a validation error from a request.
            throw new PayloadValidationException("failed over dummy validator");
        }
    }
}
