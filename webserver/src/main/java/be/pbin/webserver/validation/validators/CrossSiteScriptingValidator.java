package be.pbin.webserver.validation.validators;


import be.pbin.webserver.validation.PayloadValidationException;
import be.pbin.webserver.validation.PayloadValidator;
import org.springframework.stereotype.Component;

@Component
class CrossSiteScriptingValidator implements PayloadValidator {
    @Override
    public void validate(String payload) throws PayloadValidationException {
        //todo
    }
}
