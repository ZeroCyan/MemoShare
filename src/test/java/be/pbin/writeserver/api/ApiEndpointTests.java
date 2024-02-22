package be.pbin.writeserver.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiEndpointTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void when_unknownRequestPath_shouldSend404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/lkajajeijfijfieja", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        response = restTemplate.postForEntity("/api/lkajajeijfijfieja", new NoteData(""), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
