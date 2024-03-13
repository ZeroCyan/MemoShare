package be.pbin.webserver;

import be.pbin.webserver.api.Note;
import be.pbin.webserver.client.exceptions.HttpClientException;
import be.pbin.webserver.client.HttpClientServiceImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PostEndpointValidationTests {

    private static final String BAD_REQUEST_MESSAGE_TEMPLATE = """
            {"type":"about:blank","title":"Bad Request","status":400,"detail":"%s","instance":"/api/note"}""";
    private static String INVALID_REQUEST_MESSAGE;
    private static String INVALID_ARGUMENT_MESSAGE;
    private static final String POST_ENDPOINT = "/api/note";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MessageSource messageSource;

    @MockBean
    private HttpClientServiceImpl httpClientService;

    @BeforeEach
    void init() {
        INVALID_REQUEST_MESSAGE = BAD_REQUEST_MESSAGE_TEMPLATE.formatted(messageSource.getMessage("problemDetail.org.springframework.http.converter.HttpMessageNotReadableException", null, Locale.getDefault()));
        INVALID_ARGUMENT_MESSAGE = BAD_REQUEST_MESSAGE_TEMPLATE.formatted(messageSource.getMessage("problemDetail.org.springframework.web.bind.MethodArgumentNotValidException", null, Locale.getDefault()));
    }

    @Test
    void requestBody_trailingComma_shouldReturn400() throws JSONException {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "dummy note content",
                }
                """;

        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        System.out.println(response.getBody());
        JSONAssert.assertEquals(INVALID_REQUEST_MESSAGE, response.getBody(), true);
    }

    @Test
    void requestBody_orderOfArrayElementsChanged_shouldPass() throws HttpClientException {
        String requestBody = """
                {
                  "note_contents": "dummy note content",
                  "expiration_time_in_minutes": 3
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        URI location = URI.create(RandomStringUtils.randomAlphanumeric(10));
        ResponseEntity<String> mockResponse = ResponseEntity.created(location).build();

        Note note = new Note(3, "dummy note content");
        when(httpClientService.post(note)).thenReturn(mockResponse);

        ResponseEntity<Void> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isEqualTo(location);

        verify(httpClientService, times(1)).post(note);
    }

    @Test
    void requestBody_empty_shouldReturn400() {
        String requestBody = """
                {}
                """;

        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isEqualTo(INVALID_ARGUMENT_MESSAGE);
    }

    @Test
    void requestBody_unknownElements_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "dummy note content",
                  "foo": "bar",
                  "faa": 303
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);
    }

    @Test
    void expirationTimeValue_containsIllegalCharacters_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": "asdf",
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);
    }

    @Test
    void expirationTimeValue_negative_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": -3,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_ARGUMENT_MESSAGE);
    }

    @Test
    void expirationTimeValue_tooLarge_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 2147483646,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_ARGUMENT_MESSAGE);
    }

    @Test
    void expirationTimeKey_incorrectSpelling_shouldReturn400() {
        String requestBody = """
                {
                  "exparition_time_in_minutes": 1,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);
    }

    @Test
    void expirationTimeKey_notPresent_shouldReturn400() {
        String requestBody = """
                {
                  "e": 1,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);

        ///

        requestBody = """
                {
                  "": 1,
                  "note_contents": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);

        ///

        requestBody = """
                {
                  "asdjfiow": 1,
                  "note_contents": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);

    }

    @Test
    void payloadKey_incorrectSpelling_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "note_contnts": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);

        ///

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "post_contents": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);

        ///

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "notecontents": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);

        ///

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);
    }

    @Test
    void payloadKey_absence_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "p": "dummy note content"
                }
                """;

        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "rueiroew59202": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_REQUEST_MESSAGE);
    }

    @Test
    void payloadValue_empty_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": ""
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_ARGUMENT_MESSAGE);
    }

    @Test
    void payloadValue_tooLarge_shouldReturn400() {
        String tooLargeString = RandomStringUtils.randomAlphanumeric(1_000_001);

        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "%s"
                }
                """.formatted(tooLargeString);

        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(INVALID_ARGUMENT_MESSAGE);
    }

    @Test
    void saveRequest_incorrectMimeType_XML_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "dummy note content"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);

        String expectedResponseBody = """
                {"type":"about:blank","title":"Unsupported Media Type","status":415,"detail":"Content-Type 'application/xml' is not supported.","instance":"/api/note"}""";
        assertThat(response.getBody()).isEqualTo(expectedResponseBody);
    }

    @Test
    void saveRequest_incorrectMimeType_JPEG_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "dummy note content"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        String expectedResponseBody = """
                {"type":"about:blank","title":"Unsupported Media Type","status":415,"detail":"Content-Type 'image/jpeg' is not supported.","instance":"/api/note"}""";
        assertThat(response.getBody()).isEqualTo(expectedResponseBody);
    }

    @Test
    void test_saveRequest_requestHeader_notUTF8_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "dummy note content"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", List.of("application/json;charset=UTF-16"));
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isEqualTo("""
                {"type":"about:blank","title":"Unsupported Media Type","status":415,"detail":"Content-Type 'application/json;charset=UTF-16' is not supported.","instance":"/api/note"}""");
    }

    private HttpEntity<String> createHttpEntity(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }
}
