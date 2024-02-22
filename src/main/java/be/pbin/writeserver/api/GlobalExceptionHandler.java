package be.pbin.writeserver.api;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleUnsupportedHttpMime(HttpMediaTypeNotSupportedException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableExceptions(HttpMessageNotReadableException exception) {
        //todo: Dirty solution. Hacky, works for now, but don't lose sight of the sword of technical debt hanging over your head
        // context: Some error messages are custom and provided with annotations on noteData, others derived from Jackson internals.
        // some messages in these exceptions reveal internals of application (e.g. fully qualified class names)
        Throwable cause = exception.getCause();
        if (cause instanceof UnrecognizedPropertyException || cause instanceof JsonParseException) {
            return ResponseEntity.badRequest().body("Errors in request body detected: Refer to the API contract for the correct request body format.");
        }
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(MismatchedInputException.class)
    public ResponseEntity<String> handleMismatchedInputExceptions(MismatchedInputException exception) {
        return ResponseEntity.badRequest().body("Input parsing error: Refer to the API contract for the correct request body format.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();
        for (FieldError fieldError : result.getFieldErrors()) {
            errorMessage.append("Input validation error: ");
            errorMessage.append(fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errorMessage.toString());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException exception) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        ex.printStackTrace(); //todo: use a logging framework
        return ResponseEntity.internalServerError().body("An unexpected error occurred.");
    }
}