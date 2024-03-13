package be.pbin.webserver;

import be.pbin.webserver.client.HttpClientServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ReadServerIntegrationTests {

    private static final String WEB_SERVER_GET_ENDPOINT = "/api/note";
    private static final String SHORTLINK_REQ_PARAM = "?shortlink=";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HttpClientServiceImpl httpClientService;

    @Autowired
    private MessageSource messageSource;

    @Test
    void readServerReturns200_shouldReturn200() throws Exception {
        String body = """
                {
                  "created_at": "2024-02-20T15:30:45",
                  "note_contents": "dummy note content"
                }
                """;

        ResponseEntity<String> response = executeGetRequestAndReturnStatusCode(200, body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), body);
    }

    @Test
    void readServerReturns404_shouldReturn404() throws Exception {
        ResponseEntity<String> response = executeGetRequestAndReturnStatusCode(404, "");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        String expectedBody = """
                {"type":"about:blank","title":"Not Found","status":404,"detail":"%s","instance":"/api/note"}"""
                .formatted(messageSource.getMessage("memoshare.error.not.found", null, Locale.getDefault()));

        assertEquals(expectedBody, response.getBody());
    }

    /**
     * If the ReadServer returns any of these (or other) response codes, something went wrong in the contract between Web- and ReadServer,
     * or the ReadServer is in an error state.
     * Bad requests by the user are already filtered out by the REST API validation (cf. {@link GetEndpointValidationTests} ).
     */
    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 405, 406, 429, 500})
    void readServerReturnsX_shouldReturn500(int statusCode) throws Exception {
        ResponseEntity<String> response = executeGetRequestAndReturnStatusCode(statusCode, Strings.EMPTY);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        String expectedBody = """
                                {"type":"about:blank","title":"Internal Server Error","status":500,"detail":"%s","instance":"/api/note"}"""
                                .formatted(messageSource.getMessage("memoshare.error.internal.server", null, Locale.getDefault()));

        assertEquals(expectedBody, response.getBody());
    }

    private ResponseEntity<String> executeGetRequestAndReturnStatusCode(int httpResponseCode, String responseBody) throws Exception {
        MockWebServer readServer = new MockWebServer();

        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(httpResponseCode);
        if (!responseBody.isEmpty()) {
            mockResponse.setBody(responseBody);
        }

        readServer.enqueue(mockResponse);

        // Overwrite base url defined in test/resources/application-test.yaml through reflection
        Field readServerBaseUrl = httpClientService.getClass().getDeclaredField("READ_SERVER_BASE_URL");
        readServerBaseUrl.setAccessible(true);
        readServerBaseUrl.set(httpClientService, readServer.url("").toString());

        // request to webserver
        String shortlink = RandomStringUtils.randomAlphanumeric(8);
        ResponseEntity<String> response = restTemplate.getForEntity(WEB_SERVER_GET_ENDPOINT + SHORTLINK_REQ_PARAM + shortlink, String.class);

        // verify request to readserver
        RecordedRequest request = readServer.takeRequest();
        assertEquals("GET", request.getMethod());
        // double slash because MockWebServer adds slash to base url
        assertEquals("//api/get/" + shortlink, request.getPath());
        assertEquals(String.format("http://localhost:%s//api/get/%s", readServer.getPort(), shortlink), request.getRequestUrl().toString());

        //
        assertThat(request.getHeader("Accept")).contains(MediaType.APPLICATION_JSON_VALUE);
        readServer.close();
        return response;
    }

}
