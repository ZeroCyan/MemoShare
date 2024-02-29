package be.pbin.webserver.client;

public class HttpClientRequestFailureException extends Exception {

    public HttpClientRequestFailureException(String message) {
        super(message);
    }

    public HttpClientRequestFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
