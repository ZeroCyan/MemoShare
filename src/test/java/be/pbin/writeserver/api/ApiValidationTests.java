package be.pbin.writeserver.api;

import be.pbin.writeserver.data.sql.PasteModel;
import be.pbin.writeserver.data.sql.SQLRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiValidationTests {

    private static final String POST_ENDPOINT = "/api/paste";
    private static final String PARSE_ERROR_MESSAGE = "Errors in request body detected: Refer to the API contract for the correct request body format.";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SQLRepository sqlRepository;

    @Test //todo: dirty test, clean it up
    void whenTheExpirationTimeInMinutesIsNotPresentThereShouldBeNoExpiration() {
        PasteData newPasteData = new PasteData("please don't let me expire");
        ResponseEntity<String> response = restTemplate.postForEntity(POST_ENDPOINT, newPasteData, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI returnedUri = response.getHeaders().getLocation();

        String path = returnedUri.getPath();
        String[] segments = path.split("/");
        String lastSegment = segments[segments.length - 1];

        assertTrue(sqlRepository.existsById(lastSegment));
        Optional<PasteModel> pasteModelOptional = sqlRepository.findById(lastSegment);

        assertThat(pasteModelOptional.get().getExpirationTime()).isEqualTo(0);
    }

    @Test
    void test_properRequest_shouldPass() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "paste_contents": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_requestHasIncorrectMimeType_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "paste_contents": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Content-Type 'application/xml' is not supported");

        ///

        headers.setContentType(MediaType.IMAGE_JPEG);

        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Content-Type 'image/jpeg' is not supported");
    }

    @Test
    void test_requestBody_trailingComma_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "paste_contents": "dummy paste content",
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_requestBody_orderOfArrayElementsChanged_shouldPass() {
        String requestBody = """
                {
                  "paste_contents": "dummy paste content",
                  "expiration_time_in_minutes": 3
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_requestBody_empty_shouldReturn400() {
        String requestBody = """
                {}
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Input validation error: 'paste_contents' must be present in the request. Hint: check spelling");
    }

    @Test
    void test_requestBody_unknownElements_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "paste_contents": "dummy paste content",
                  "foo": "bar",
                  "faa": 303
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_expirationTimeValue_containsIllegalCharacters_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": "asdf",
                  "paste_contents": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("JSON parse error: Cannot deserialize value of type `int` from String \"asdf\": not a valid `int` value");
    }

    @Test
    void test_expirationTimeValue_negative_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": -3,
                  "paste_contents": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Input validation error: Expiration time cannot be negative");
    }

    @Test
    void test_expirationTimeValue_tooLarge_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 2147483646,
                  "paste_contents": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Input validation error: Expiration time exceeds limit. Hint: Set expiration to 0 to prevent expiration.");
    }

    @Test
    void test_expirationTimeKey_incorrectSpelling_shouldReturn400() {
        String requestBody = """
                {
                  "exparition_time_in_minutes": 1,
                  "paste_contents": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_expirationTimeKey_notPresent_shouldReturn400() {
        String requestBody = """
                {
                  "e": 1,
                  "paste_contents": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        ///

        requestBody = """
                {
                  "": 1,
                  "paste_contents": "dummy paste content"
                }
                """;
        entity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        ///

        requestBody = """
                {
                  "asdjfiow": 1,
                  "paste_contents": "dummy paste content"
                }
                """;
        entity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

    }

    @Test
    void test_pasteContentsKey_incorrectSpelling_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "paste_contnts": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        ///

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "post_contents": "dummy paste content"
                }
                """;
        entity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        ///

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "pastecontents": "dummy paste content"
                }
                """;
        entity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        ///

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "": "dummy paste content"
                }
                """;
        entity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_pasteContentsKey_absence_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "p": "dummy paste content"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "": "dummy paste content"
                }
                """;
        entity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "rueiroew59202": "dummy paste content"
                }
                """;
        entity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_pasteContentValue_empty_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "paste_contents": ""
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Input validation error: 'paste_contents' is empty");
    }

//    @Test //todo: results in a JSON parse error: Illegal unquoted character ((CTRL-CHAR, code 18)): has to be escaped using backslash to be included in string value
            //todo: later on check if the parsing of certain characters gives errors with JSON
    @Test
    void test_pasteContentValue_tooLarge_shouldReturn400() {
        String tooLargeString = RandomStringUtils.randomAlphanumeric(1_000_001);

        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "paste_contents": \"%s\"
                }
                """.formatted(tooLargeString);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Input validation error: Character limit exceeded. The maximum allowed is 1 million characters.");
    }
}
