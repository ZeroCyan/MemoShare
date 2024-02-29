package be.pbin.webserver.client;

public class NoNoteFoundException extends HttpClientRequestFailureException {

    public NoNoteFoundException(String message) {
        super(message);
    }
}
