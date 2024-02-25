package be.pbin.writeserver.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiEndpointTests {

    private static final String POST_ENDPOINT = "/api/note";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void when_unknownRequestPath_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/lkajajeijfijfieja", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        response = restTemplate.postForEntity("/api/lkajajeijfijfieja", new NoteDTO(""), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void test_requestHeader_UTF8_shouldPass(){
        String requestBody = """
                {
                  "expiration_time_in_minutes": 3,
                  "note_contents": "dummy note content"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", List.of("application/json;charset=UTF-8"));
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_requestHeader_notUTF8_shouldReturn400(){
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Content-Type 'application/json;charset=UTF-16' is not supported");
    }

    @Test
    void test_requestHasIncorrectMimeType_shouldReturn400() {
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Content-Type 'application/xml' is not supported");

        ///

        headers.setContentType(MediaType.IMAGE_JPEG);
        entity = new HttpEntity<>(requestBody, headers);

        response = restTemplate.exchange(POST_ENDPOINT, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Content-Type 'image/jpeg' is not supported");
    }
}
