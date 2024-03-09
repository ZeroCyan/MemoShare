package be.pbin.webserver.validation;

import be.pbin.webserver.client.HttpClientRequestFailureException;
import be.pbin.webserver.client.HttpClientService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetEndpointValidationTests {

    private static final String GET_ENDPOINT = "/api/note";
    private static final String PARAM_ERROR_MESSAGE = "The 'shortlink' request parameter must be 8 characters long and consist of alphanumeric characters.";
    private static final String SHORTLINK_REQ_PARAM = "?shortlink=";

    @MockBean
    private HttpClientService httpClientService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void happyPath() throws HttpClientRequestFailureException {
        String body = """
                {
                  "created_at": "2024-02-20T15:30:45",
                  "note_contents": "dummy note content"
                }
                """;
        ResponseEntity<String> response = new ResponseEntity<>(body, HttpStatus.OK);
        String shortlink = RandomStringUtils.randomAlphanumeric(8);
        when(httpClientService.get(shortlink)).thenReturn(response);

        ResponseEntity<String> actual = restTemplate.getForEntity(GET_ENDPOINT + SHORTLINK_REQ_PARAM + shortlink, String.class);
        assertThat(actual.getBody()).isEqualTo(response.getBody());
        assertThat(actual.getStatusCode()).isEqualTo(response.getStatusCode());
        verify(httpClientService, times(1)).get(shortlink);
        verifyNoMoreInteractions(httpClientService);
    }
    
    @Test
    void unknownEndpoint_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/unkown", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unknownPath_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/save/extra/path", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void requestParam_notPresent_shouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(GET_ENDPOINT, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Required request parameter 'shortlink' for method parameter type String is not present");
    }

    @Test
    void requestParamKey_wrongParam_shouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(GET_ENDPOINT + "?wrongparam=12345678", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Required request parameter 'shortlink' for method parameter type String is not present");
    }

    @Test
    void requestParamValue_absent_shouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(GET_ENDPOINT + SHORTLINK_REQ_PARAM, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARAM_ERROR_MESSAGE);
    }

    @Test
    void requestParamValue_incorrectSize_tooShort_shouldReturn400() {
        String INVALID_REQUEST_PARAM = RandomStringUtils.randomAlphanumeric(6);
        ResponseEntity<String> response = restTemplate.getForEntity(GET_ENDPOINT + SHORTLINK_REQ_PARAM + INVALID_REQUEST_PARAM, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARAM_ERROR_MESSAGE);
    }

    @Test
    void requestParamValue_incorrectSize_tooLong_shouldReturn400() {
        String INVALID_REQUEST_PARAM = RandomStringUtils.randomAlphanumeric(16);
        ResponseEntity<String> response = restTemplate.getForEntity(GET_ENDPOINT + SHORTLINK_REQ_PARAM + INVALID_REQUEST_PARAM, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARAM_ERROR_MESSAGE);
    }

    @Test
    void requestParamValue_notAlphaNumeric_shouldReturn400() {
        String INVALID_REQUEST_PARAM = "123456#8";
        ResponseEntity<String> response = restTemplate.getForEntity(GET_ENDPOINT + SHORTLINK_REQ_PARAM + INVALID_REQUEST_PARAM, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARAM_ERROR_MESSAGE);
    }

    @Test
    void request_doesNotAcceptJson_shouldReturn406() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_ATOM_XML));
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(GET_ENDPOINT, HttpMethod.GET, httpEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
        assertThat(response.getBody()).isEqualTo("We cannot provide a response matching any of the requested content types. Available content types are: JSON");
    }
}
