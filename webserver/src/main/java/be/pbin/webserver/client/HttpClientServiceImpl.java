package be.pbin.webserver.client;

import be.pbin.webserver.api.Note;
import be.pbin.webserver.validation.PayloadValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
                .timeout(Duration.of(10, SECONDS))
                .build();

        HttpResponse<String> response = executeRequest(request);

        if (response.statusCode() == 404) {
            throw new NoNoteFoundException("We couldn't locate the file you are looking for. Perhaps it has expired.");
        }

        if (response.statusCode() == 200 && response.body() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(response.body());
        }

        log.error("Http POST request to read server failed with status code {}. Note id: {}", response.statusCode(), noteId);
        throw new HttpClientRequestFailureException("HTTP POST request failed with status code: " + response.statusCode());
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
        throw new HttpClientRequestFailureException("POST request failed with status code: " + response.statusCode());
    }

    private String serializeNote(Note note) throws HttpClientRequestFailureException {
        try {
            return serializer.writeValueAsString(note);
        } catch (JsonProcessingException exception) {
            log.error("Could not serialize note: {}", note, exception);
            throw new HttpClientRequestFailureException("Could not serialize note.");
        }
    }

    private HttpResponse<String> executeRequest(HttpRequest request) throws HttpClientRequestFailureException {
        HttpResponse<String> response = null;

        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, ofString());
        } catch (IOException exception) {
            log.error("An IO error occurred during a HTTP request: {}", exception.getMessage(), exception);
            throw new HttpClientRequestFailureException("Failed to execute HTTP request due to IO error: " + exception.getMessage(), exception);
        } catch (InterruptedException exception) {
            log.error("An interrupted exception occurred during a HTTP request: {}", exception.getMessage(), exception);
            //todo: handle the generating and handling of exception messages better
            throw new HttpClientRequestFailureException("Failed to execute HTTP request due to interupted exception: " + exception.getMessage(), exception);
        }

        if (response == null) {
            log.error("An error occurred during a HTTP request, response object is null.");
            throw new HttpClientRequestFailureException("HTTP Post request error: null response object.");
        }

        return response;
    }

    private String extractLocationFromHeaders(HttpResponse<String> response) throws HttpClientRequestFailureException {
        java.net.http.HttpHeaders headers = response.headers();

        if (headers == null || headers.map().isEmpty()) {
            log.error("POST request returned status 201 but no headers present. Response: {}", response.toString());
            throw new HttpClientRequestFailureException("POST request status code 201, but no headers present");
        }

        Optional<String> location = headers.firstValue("Location");

        if (location.isEmpty() || location.get().isEmpty()) {
            log.error("POST request returned status 201 but without Location in the header. Response: {}", response);
            throw new HttpClientRequestFailureException("POST request status code 201, but no Location in header present");
        }

        return location.get();
    }
}
