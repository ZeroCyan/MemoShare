package be.pbin.webserver;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GetEndpointValidationTests {

    private static final String WEB_SERVER_GET_ENDPOINT = "/api/note";
    private static final String PARAM_ERROR_MESSAGE = """
            {"type":"about:blank","title":"Bad Request","status":400,"detail":"It looks like theres an issue with the data you provided. Please review your input and try again.","instance":"/api/note"}""";
    private static final String SHORTLINK_REQ_PARAM = "?shortlink=";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void unknownEndpoint_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/unkown", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unknownPath_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/save/unkown/path", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void requestParam_notPresent_shouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(WEB_SERVER_GET_ENDPOINT, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("""
                {"type":"about:blank","title":"Bad Request","status":400,"detail":"Required parameter 'shortlink' is not present.","instance":"/api/note"}""");
    }

    @Test
    void requestParamKey_wrongParam_shouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(WEB_SERVER_GET_ENDPOINT + "?wrongparam=12345678", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("""
                {"type":"about:blank","title":"Bad Request","status":400,"detail":"Required parameter 'shortlink' is not present.","instance":"/api/note"}""");
    }

    @Test
    void requestParamValue_absent_shouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(WEB_SERVER_GET_ENDPOINT + SHORTLINK_REQ_PARAM, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARAM_ERROR_MESSAGE);
    }

    @Test
    void requestParamValue_tooShort_shouldReturn400() {
        String INVALID_REQUEST_PARAM = RandomStringUtils.randomAlphanumeric(6);
        ResponseEntity<String> response = restTemplate.getForEntity(WEB_SERVER_GET_ENDPOINT + SHORTLINK_REQ_PARAM + INVALID_REQUEST_PARAM, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARAM_ERROR_MESSAGE);
    }

    @Test
    void requestParamValue_tooLong_shouldReturn400() {
        String INVALID_REQUEST_PARAM = RandomStringUtils.randomAlphanumeric(16);
        ResponseEntity<String> response = restTemplate.getForEntity(WEB_SERVER_GET_ENDPOINT + SHORTLINK_REQ_PARAM + INVALID_REQUEST_PARAM, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARAM_ERROR_MESSAGE);
    }

    @Test
    void requestParamValue_notAlphaNumeric_shouldReturn400() {
        String INVALID_REQUEST_PARAM = "123456#8";
        ResponseEntity<String> response = restTemplate.getForEntity(WEB_SERVER_GET_ENDPOINT + SHORTLINK_REQ_PARAM + INVALID_REQUEST_PARAM, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(PARAM_ERROR_MESSAGE);
    }

    @Test
    void request_doesNotAcceptJson_shouldReturn406() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_ATOM_XML));
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(WEB_SERVER_GET_ENDPOINT, HttpMethod.GET, httpEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
        assertThat(response.getBody()).isEqualTo("""
                {"type":"about:blank","title":"Not Acceptable","status":406,"detail":"Acceptable representations: [application/json].","instance":"/api/note"}""");
    }
}
