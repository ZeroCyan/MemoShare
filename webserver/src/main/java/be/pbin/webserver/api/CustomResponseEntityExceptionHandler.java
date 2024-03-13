package be.pbin.webserver.api;

import be.pbin.webserver.client.exceptions.HttpClientException;
import be.pbin.webserver.client.exceptions.HttpClientResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    // Extending ResponseEntityExceptionHandler allows error output to be in the Problem Detail format (cf. RFC-7807)

    @ExceptionHandler(HttpClientResourceNotFoundException.class)
    public ResponseEntity<Object> handleHttpClientResourceNotFoundException(HttpClientResourceNotFoundException exception, WebRequest request) {
        ProblemDetail body = createProblemDetail(exception, HttpStatus.NOT_FOUND, "Resource not found", "memoshare.error.not.found", null, request);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(HttpClientException.class)
    public ResponseEntity<Object> handleHttpClientRequestFailureException(HttpClientException exception, WebRequest request) {
        ProblemDetail body = createProblemDetail(exception, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "memoshare.error.internal.server", null, request);
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
