package be.pbin.webserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiEndpointTests {

    private static final String POST_ENDPOINT = "/api/save";


    @Autowired
    private TestRestTemplate restTemplate;


    //todo: more testing, also for get requests
    @Test
    void test_saveRequest_requestHeader_UTF8_shouldPass(){
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
    void test_saveRequest_requestHeader_notUTF8_shouldReturn400(){
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
    void test_saveRequest_requestHasIncorrectMimeType_shouldReturn400() {
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
