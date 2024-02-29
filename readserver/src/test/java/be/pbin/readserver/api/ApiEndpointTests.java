package be.pbin.readserver.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiEndpointTests {

    private static final String GET_ENDPOINT = "/api/get/";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void test_getNote_happyPath() {
        ResponseEntity<String> response = restTemplate.getForEntity(GET_ENDPOINT + "51cSLn0f", String.class); //This note has no expiry
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"note_contents\":\"please don't let me expire\",\"created_at\":\"2024-02-24T17:10:54\"}");
    }

    @Test
    void test_getNote_noteDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity(GET_ENDPOINT + "123", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void test_unknownPath_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/unkownpath/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}