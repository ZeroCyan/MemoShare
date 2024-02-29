package be.pbin.webserver.api;

import be.pbin.webserver.client.HttpClientRequestFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpClientRequestFailureException.class)
    public ResponseEntity<String> handleHttpClientRequestFailureException(HttpClientRequestFailureException exception) {
        return ResponseEntity.internalServerError().body("Something went wrong. Please try again later.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception exception) {
        log.error("An unexpected error occurred: {}", exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body("An unexpected error occurred. Please try again later.");
    }
}
