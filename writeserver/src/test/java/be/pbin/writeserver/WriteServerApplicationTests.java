package be.pbin.writeserver;

import be.pbin.writeserver.api.NoteDTO;
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

    private static final String POST_ENDPOINT = "/api/save";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateNewNoteObject() {
        NoteDTO newNoteDTO = new NoteDTO(10, "contents");

        ResponseEntity<String> response = restTemplate.postForEntity(POST_ENDPOINT, newNoteDTO, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI returnedUri = response.getHeaders().getLocation();
        assertThat(returnedUri).isNotNull();
    }

    @Test
    void shouldGenerateUniqueUrlForNewNote() {
        NoteDTO newNoteDTO = new NoteDTO(10, "contents");

        ResponseEntity<String> response = restTemplate.postForEntity(POST_ENDPOINT, newNoteDTO, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI returnedUri = response.getHeaders().getLocation();
        assertThat(returnedUri).isNotNull();

        String lastSegment = UriUtils.extractLastSegment(returnedUri);

        assertThat(lastSegment.length()).isEqualTo(8);
        assertTrue(lastSegment.matches("^\\w{8}$"));
    }
}
