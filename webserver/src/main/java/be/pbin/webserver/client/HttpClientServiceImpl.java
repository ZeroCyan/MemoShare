package be.pbin.webserver.client;

import be.pbin.webserver.api.Note;
import be.pbin.webserver.client.exceptions.*;
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

    private final ObjectMapper serializer;
    private final HttpClientFactory httpClientFactory;

    public HttpClientServiceImpl(ObjectMapper serializer, HttpClientFactory httpClientFactory) {
        this.serializer = serializer;
        this.httpClientFactory = httpClientFactory;
    }

    // todo: refactor, split GET and POST in separate classes

    @Override
    public ResponseEntity<String> get(String noteId) throws HttpClientException {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(READ_SERVER_BASE_URL + READ_SERVER_PATH + "/" + noteId))
                    .GET()
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .timeout(Duration.of(10, SECONDS))
                    .build();

            HttpResponse<String> response = executeRequest(request);

            int statusCode = response.statusCode();

            if (statusCode == 200 && response.body() != null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(response.body());
            }

            if (statusCode == 404) {
                log.info("Http GET request to read server failed with status code {}. Note id: {}", statusCode, noteId);
                throw new HttpClientResourceNotFoundException();
            }

            log.error("Http GET request to read server failed with status code {}. Note id: {}", statusCode, noteId);
            throw new HttpClientUnexpectedStatusCodeException();
    }

    @Override
    public ResponseEntity<String> post(Note note) throws HttpClientException {
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
            throw new HttpClientUnexpectedStatusCodeException();
    }

    private String serializeNote(Note note) throws HttpClientException {
        try {
            return serializer.writeValueAsString(note);
        } catch (JsonProcessingException exception) {
            log.error("Could not serialize note: {}", note, exception);
            throw new HttpClientSerializationException(exception);
        }
    }

    private HttpResponse<String> executeRequest(HttpRequest request) throws HttpClientException {
        HttpResponse<String> response;

        HttpClient httpClient = httpClientFactory.getHttpClient();

        try {
            response = httpClient.send(request, ofString());
        } catch (IOException | InterruptedException exception) {
            log.error("A {} occurred during a HTTP request: {}", exception.getClass().getName(), exception.getMessage(), exception);
            throw new HttpClientExecutionException(exception);
        }

        if (response == null) {
            log.error("An error occurred during a HTTP request, response object is null.");
            throw new HttpClientNoResponseException();
        }

        return response;
    }

    private String extractLocationFromHeaders(HttpResponse<String> response) throws HttpClientException {
        java.net.http.HttpHeaders headers = response.headers();

        if (headers == null || headers.map().isEmpty()) {
            log.error("POST request returned status 201 but no headers present. Response: {}", response);
            throw new HttpClientResponseHeaderException();
        }

        Optional<String> location = headers.firstValue("Location");

        if (location.isEmpty() || location.get().isEmpty()) {
            log.error("POST request returned status 201 but without Location in the header. Response: {}", response);
            throw new HttpClientResponseHeaderLocationException();
        }

        return location.get();
    }
}
