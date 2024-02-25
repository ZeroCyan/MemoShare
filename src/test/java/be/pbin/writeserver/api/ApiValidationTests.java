package be.pbin.writeserver.api;

import be.pbin.writeserver.data.metadata.MetaData;
import be.pbin.writeserver.data.metadata.MetadataRepository;
import be.pbin.writeserver.utils.UriUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiValidationTests {

    private static final String POST_ENDPOINT = "/api/note";
    private static final String PARSE_ERROR_MESSAGE = "Errors in request body detected: Refer to the API contract for the correct request body format.";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MetadataRepository sqlRepository;

    @Test //FIXME: dirty test, clean it up
    void whenTheExpirationTimeInMinutesIsNotPresentThereShouldBeNoExpiration() {
        NoteDTO newNoteDTO = new NoteDTO("please don't let me expire");
        ResponseEntity<String> response = restTemplate.postForEntity(POST_ENDPOINT, newNoteDTO, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI returnedUri = response.getHeaders().getLocation();
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders()).isNotNull();

        String lastSegment = UriUtils.extractLastSegment(returnedUri);

        assertThat(sqlRepository.existsById(lastSegment)).isTrue();
        Optional<MetaData> noteModelOptional = sqlRepository.findById(lastSegment);

        assertThat(noteModelOptional.get().getExpirationDate()).isEqualTo(LocalDateTime.of(9999, 12, 31, 0,0,0));
    }

    @Test
    void test_properRequest_shouldPass() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_requestBody_trailingComma_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "dummy note content",
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_requestBody_orderOfArrayElementsChanged_shouldPass() {
        String requestBody = """
                {
                  "note_contents": "dummy note content",
                  "expiration_time_in_minutes": 3
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_requestBody_empty_shouldReturn400() {
        String requestBody = """
                {}
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Input validation error: 'note_contents' must be present in the request. Hint: check spelling");
    }

    @Test
    void test_requestBody_unknownElements_shouldReturn400() {
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
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_expirationTimeValue_containsIllegalCharacters_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": "asdf",
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("JSON parse error: Cannot deserialize value of type `int` from String \"asdf\": not a valid `int` value");
    }

    @Test
    void test_expirationTimeValue_negative_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": -3,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Input validation error: Expiration time cannot be negative");
    }

    @Test
    void test_expirationTimeValue_tooLarge_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 2147483646,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Input validation error: Expiration time exceeds limit. Hint: Set expiration to 0 to prevent expiration.");
    }

    @Test
    void test_expirationTimeKey_incorrectSpelling_shouldReturn400() {
        String requestBody = """
                {
                  "exparition_time_in_minutes": 1,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_expirationTimeKey_notPresent_shouldReturn400() {
        String requestBody = """
                {
                  "e": 1,
                  "note_contents": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

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
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

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
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

    }

    @Test
    void test_payloadKey_incorrectSpelling_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "note_contnts": "dummy note content"
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

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
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

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
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

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
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_payloadKey_absence_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "p": "dummy note content"
                }
                """;

        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);

        requestBody = """
                {
                  "expiration_time_in_minutes": 1,
                  "rueiroew59202": "dummy note content"
                }
                """;
        entity = createHttpEntity(requestBody);
        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARSE_ERROR_MESSAGE);
    }

    @Test
    void test_payloadValue_empty_shouldReturn400() {
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": ""
                }
                """;
        HttpEntity<String> entity = createHttpEntity(requestBody);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Input validation error: 'note_contents' is empty");
    }

    //FIXME: results in a JSON parse error: Illegal unquoted character ((CTRL-CHAR, code 18)): has to be escaped using backslash to be included in string value
    //todo: encoding validation needs to be thorough!
    @Test
    void test_payloadValue_tooLarge_shouldReturn400() {
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
        assertThat(response.getBody()).isEqualTo("Input validation error: Character limit exceeded. The maximum allowed is 1 million characters.");
    }

    //NiceToHave: add test that asserts that fractional minutes are disallowed

    private HttpEntity<String> createHttpEntity(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }
}
