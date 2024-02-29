package be.pbin.readserver.data.payload;

import be.pbin.readserver.data.DataProcessingException;

public class PayloadAccessException extends DataProcessingException {
    public PayloadAccessException(String message) {
        super(message);
    }

    public PayloadAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}