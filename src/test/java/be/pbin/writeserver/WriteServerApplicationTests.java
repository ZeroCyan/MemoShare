package be.pbin.writeserver;

import be.pbin.writeserver.api.PasteData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WriteServerApplicationTests {

	@Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldCreateNewPasteObject() {
        PasteData newPasteData = new PasteData(10, "contents");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/paste", newPasteData, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI returnedUri = response.getHeaders().getLocation();
        assertThat(returnedUri).isNotNull();

        ResponseEntity<String> getResponse = restTemplate.getForEntity(returnedUri, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getHeaders()).containsKey("forId");
    }

    @Test
    void shouldGenerateUniqueUrlForNewPaste() {
        PasteData newPasteData = new PasteData(10, "contents");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/paste", newPasteData, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI returnedUri = response.getHeaders().getLocation();
        assertThat(returnedUri).isNotNull();

        String path = returnedUri.getPath();
        String[] segments = path.split("/");
        String lastSegment = segments[segments.length - 1];

        assertThat(lastSegment.length()).isEqualTo(8);
        assertTrue(lastSegment.matches("^\\w{8}$"));
    }
}
