package be.pbin.writeserver.data.payload;

import be.pbin.writeserver.data.DataProcessingException;

public class PayloadStorageException extends DataProcessingException {
    public PayloadStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}