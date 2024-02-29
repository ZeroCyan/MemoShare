package be.pbin.webserver.validation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayloadValidationServiceImpl implements PayloadValidationService {

    private final Logger log = LoggerFactory.getLogger(PayloadValidationService.class);

    private final List<PayloadValidator> validationChain;

    public PayloadValidationServiceImpl(List<PayloadValidator> validationChain) {
        this.validationChain = validationChain;
    }

    @Override
    public boolean hasValidationErrors(String payload) {
        boolean hasErrors = false;
        List<String> validationErrors = new ArrayList<>();

        for (PayloadValidator validator : validationChain) {
            try {
                validator.validate(payload);
            } catch (PayloadValidationException exception) {
                hasErrors = true;
                validationErrors.add(exception.getMessage());
            }
        }

        if (hasErrors) {
            log.warn("Validation of payload completed with errors. Errors: {} ", validationErrors);
        }
        return hasErrors;
    }
}
