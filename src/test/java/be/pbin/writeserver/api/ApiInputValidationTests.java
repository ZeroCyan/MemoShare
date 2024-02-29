package be.pbin.writeserver.api;

import be.pbin.writeserver.data.metadata.MetaData;
import be.pbin.writeserver.data.MetadataRepository;
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
public class ApiInputValidationTests {

    private static final String POST_ENDPOINT = "/api/save";
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

    private HttpEntity<String> createHttpEntity(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }

}
