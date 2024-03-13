package be.pbin.webserver.client.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class HttpClientSerializationException extends HttpClientException {
    public HttpClientSerializationException(JsonProcessingException exception) {
        super(exception);
    }
}

