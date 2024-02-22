package be.pbin.writeserver;

import be.pbin.writeserver.api.NoteData;
import be.pbin.writeserver.utils.UriUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WriteServerApplicationTests {

	@Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateNewNoteObject() {
        NoteData newNoteData = new NoteData(10, "contents");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/note", newNoteData, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI returnedUri = response.getHeaders().getLocation();
        assertThat(returnedUri).isNotNull();

        //todo: temporary solution, since the GET note endpoint will be part of the readserver
        ResponseEntity<String> getResponse = restTemplate.getForEntity(returnedUri, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getHeaders()).containsKey("forId");
    }

    @Test
    void shouldGenerateUniqueUrlForNewNote() {
        NoteData newNoteData = new NoteData(10, "contents");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/note", newNoteData, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI returnedUri = response.getHeaders().getLocation();
        assertThat(returnedUri).isNotNull();

        String lastSegment = UriUtils.extractLastSegment(returnedUri);

        assertThat(lastSegment.length()).isEqualTo(8);
        assertTrue(lastSegment.matches("^\\w{8}$"));
    }
}
