package be.pbin.webserver.client;

import be.pbin.webserver.api.Note;
import be.pbin.webserver.validation.PayloadValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class HttpClientServiceImpl implements HttpClientService {

    private static final Logger log = LoggerFactory.getLogger(HttpClientServiceImpl.class);

    @Value("${be.pbin.write-server.base-url}")
    private String WRITE_SERVER_BASE_URL;
    @Value("${be.pbin.write-server.path}")
    private String WRITE_SERVER_PATH;
    @Setter
    @Getter
    @Value("${be.pbin.read-server.base-url}")
    private String READ_SERVER_BASE_URL;
    @Value("${be.pbin.read-server.path}")
    private String READ_SERVER_PATH;

    private final PayloadValidationService validationService;
    private final ObjectMapper serializer;

    public HttpClientServiceImpl(PayloadValidationService validationService, ObjectMapper serializer) {
        this.validationService = validationService;
        this.serializer = serializer;
    }

    @Override
    public ResponseEntity<String> get(String noteId) throws HttpClientRequestFailureException {
        String URL = READ_SERVER_BASE_URL + READ_SERVER_PATH + "/" + noteId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET()
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .timeout(Duration.of(10, SECONDS))
                .build();

        HttpResponse<String> response = executeRequest(request);

        int statusCode = response.statusCode();

        if (statusCode == 200 && response.body() != null) {
            String responseBody = HtmlUtils.htmlEscape(response.body()); // Prevent XSS attacks
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(responseBody);
        }

        if (statusCode == 404) {
            log.warn("Http GET request to read server failed with status code {}. Note id: {}", statusCode, noteId);
            return ResponseEntity.notFound().build();
        }

        log.error("Http GET request to read server failed with status code {}. Note id: {}", statusCode, noteId);
        return ResponseEntity.internalServerError().body("An internal server error occurred. Please try again later.");
    }

    @Override
    public ResponseEntity<Void> post(Note note) throws HttpClientRequestFailureException {
        if (validationService.hasValidationErrors(note.getNoteContent())) {
            return ResponseEntity.badRequest().build();
        }

        String serializedNote = serializeNote(note);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(WRITE_SERVER_BASE_URL + WRITE_SERVER_PATH))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(serializedNote))
                .timeout(Duration.of(20, SECONDS))
                .build();

        HttpResponse<String> response = executeRequest(request);

        if (response.statusCode() == 201) {
            return ResponseEntity
                    .created(URI.create(extractLocationFromHeaders(response)))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }

        log.error("POST request to read server failed with status code {}.", response.statusCode());
        throw new HttpClientRequestFailureException();
    }

    private String serializeNote(Note note) throws HttpClientRequestFailureException {
        try {
            return serializer.writeValueAsString(note);
        } catch (JsonProcessingException exception) {
            log.error("Could not serialize note: {}", note, exception);
            throw new HttpClientRequestFailureException();
        }
    }

    private HttpResponse<String> executeRequest(HttpRequest request) throws HttpClientRequestFailureException {
        HttpResponse<String> response;

        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, ofString());
        } catch (IOException exception) {
            log.error("An IO error occurred during a HTTP request: {}", exception.getMessage(), exception);
            throw new HttpClientRequestFailureException();
        } catch (InterruptedException exception) {
            log.error("An interrupted exception occurred during a HTTP request: {}", exception.getMessage(), exception);
            throw new HttpClientRequestFailureException();
        }

        if (response == null) {
            log.error("An error occurred during a HTTP request, response object is null.");
            throw new HttpClientRequestFailureException();
        }

        return response;
    }

    private String extractLocationFromHeaders(HttpResponse<String> response) throws HttpClientRequestFailureException {
        java.net.http.HttpHeaders headers = response.headers();

        if (headers == null || headers.map().isEmpty()) {
            log.error("POST request returned status 201 but no headers present. Response: {}", response);
            throw new HttpClientRequestFailureException();
        }

        Optional<String> location = headers.firstValue("Location");

        if (location.isEmpty() || location.get().isEmpty()) {
            log.error("POST request returned status 201 but without Location in the header. Response: {}", response);
            throw new HttpClientRequestFailureException();
        }

        return location.get();
    }
}
