package be.pbin.writeserver.service;

import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.validation.PayloadValidationException;

public interface ValidationService {

    void validate(Payload payload) throws PayloadValidationException;
}
