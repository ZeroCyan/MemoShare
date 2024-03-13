package be.pbin.webserver;

import be.pbin.webserver.api.CustomResponseEntityExceptionHandler;
import be.pbin.webserver.api.Note;
import be.pbin.webserver.client.HttpClientFactory;
import be.pbin.webserver.client.HttpClientServiceImpl;
import be.pbin.webserver.client.exceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WriteServerIntegrationTests {
    private static final String REQUEST_BODY = """
            {
              "expiration_time_in_minutes": "2",
              "note_contents": "dummy note content"
            }
            """;

    @Value("${be.pbin.write-server.base-url}") //getter and setter cf. IntegrationTests
    private String WRITE_SERVER_BASE_URL;
    @Value("${be.pbin.write-server.path}")
    private String WRITE_SERVER_PATH;

    private final static String WEB_SERVER_PATH = "/api/note";
    private static final String INTERNAL_SERVER_ERROR_TEMPLATE = """
                        {"type":"about:blank","title":"Internal Server Error","status":500,"detail":"%s","instance":"/api/note"}""";
    private String INTERNAL_SERVER_ERROR_PROBLEM_DETAIL;

    @Autowired private TestRestTemplate restTemplate;

    @Autowired private MessageSource messageSource;

    @SpyBean private HttpClientServiceImpl httpClientService;

    @SpyBean private HttpClientFactory httpClientFactory;

    @SpyBean private ObjectMapper spySerializer;

    @SpyBean private CustomResponseEntityExceptionHandler exceptionHandler;

    @BeforeEach
    void init() {
        INTERNAL_SERVER_ERROR_PROBLEM_DETAIL = INTERNAL_SERVER_ERROR_TEMPLATE.formatted(messageSource.getMessage("memoshare.error.internal.server", null, Locale.getDefault()));

    }

    //todo: timeout tests

    @Test
    void writeServerReturns201_shouldReturn201 () throws Exception {
        MockResponse mockWriteServerResponse = new MockResponse();
        mockWriteServerResponse.setResponseCode(201);
        mockWriteServerResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        mockWriteServerResponse.setHeader(HttpHeaders.LOCATION, "someLocation");

        ResponseEntity<String> response = executePostRequestAndReturn(mockWriteServerResponse);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(URI.create("someLocation"), response.getHeaders().getLocation());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void writeServerSerializationFails_shouldReturn500 () throws Exception {
        // Serialization failure is not related to user input, thus 500
        doThrow(JsonProcessingException.class).when(spySerializer).writeValueAsString(any(Note.class));

        MockResponse mockResponse = new MockResponse();
        mockResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        mockResponse.setResponseCode(200);

        ResponseEntity<String> response = executePostRequestAndReturn(mockResponse);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_PROBLEM_DETAIL, response.getBody());

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(spySerializer, times(1)).writeValueAsString(noteCaptor.capture());
        Note capturedNote = noteCaptor.getValue();
        assertEquals(2, capturedNote.getExpirationTimeInMinutes());
        assertEquals("dummy note content", capturedNote.getNoteContent());

        verify(exceptionHandler, times(1)).handleHttpClientRequestFailureException(any(HttpClientSerializationException.class), any(WebRequest.class));
    }

    @Test
    void writeServerRequest_ThrowsIOException_shouldReturn500 () throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(httpClientFactory.getHttpClient()).thenReturn(mockHttpClient);

        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(new IOException());

        ResponseEntity<String> response = executePostRequestAndReturn(new MockResponse());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_PROBLEM_DETAIL, response.getBody());

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient, times(1)).send(httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString()));
        HttpRequest capturedRequest = httpRequestCaptor.getValue();
        assertEquals("/" +WRITE_SERVER_PATH, capturedRequest.uri().getPath());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, capturedRequest.headers().firstValue(HttpHeaders.CONTENT_TYPE).get());

        verify(exceptionHandler, times(1)).handleHttpClientRequestFailureException(any(HttpClientExecutionException.class), any(WebRequest.class));
    }

    @Test
    void writeServerRequest_ThrowsInterruptedException_shouldReturn500 () throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(httpClientFactory.getHttpClient()).thenReturn(mockHttpClient);
        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(new InterruptedException());


        ResponseEntity<String> response = executePostRequestAndReturn(new MockResponse());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_PROBLEM_DETAIL, response.getBody());

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient, times(1)).send(httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString()));

        HttpRequest capturedRequest = httpRequestCaptor.getValue();
        assertEquals("/" + WRITE_SERVER_PATH, capturedRequest.uri().getPath());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, capturedRequest.headers().firstValue(HttpHeaders.CONTENT_TYPE).get());

        verify(exceptionHandler, times(1)).handleHttpClientRequestFailureException(any(HttpClientExecutionException.class), any(WebRequest.class));
    }

    @Test
    void writeServerRequest_noResponse_shouldReturn500 () throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(httpClientFactory.getHttpClient()).thenReturn(mockHttpClient);
        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(null);

        ResponseEntity<String> response = executePostRequestAndReturn(new MockResponse());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_PROBLEM_DETAIL, response.getBody());

        verify(exceptionHandler, times(1)).handleHttpClientRequestFailureException(any(HttpClientNoResponseException.class), any(WebRequest.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 405, 406, 429, 500})
    void writeServerReturnsX_shouldReturn500(int responseCode) throws Exception {
        ResponseEntity<String> actualResponse = executePostRequestAndReturn(new MockResponse().setResponseCode(responseCode));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_PROBLEM_DETAIL, actualResponse.getBody());

        verify(exceptionHandler, times(1)).handleHttpClientRequestFailureException(any(HttpClientUnexpectedStatusCodeException.class), any(WebRequest.class));
    }

    @Test
    void writeServerReturns201_noHeaders_shouldReturn500() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(httpClientFactory.getHttpClient()).thenReturn(mockHttpClient);

        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.headers()).thenReturn(null);

        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);
        ResponseEntity<String> actualResponse = executePostRequestAndReturn(new MockResponse());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_PROBLEM_DETAIL, actualResponse.getBody());

        verify(exceptionHandler, times(1)).handleHttpClientRequestFailureException(any(HttpClientResponseHeaderException.class), any(WebRequest.class));
    }

    @Test
    void writeServerReturns201_emptyHeaders_shouldReturn500() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(httpClientFactory.getHttpClient()).thenReturn(mockHttpClient);

        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(201);

        java.net.http.HttpHeaders mockHttpHeaders = mock(java.net.http.HttpHeaders.class);
        when(mockHttpHeaders.map()).thenReturn(new HashMap<>());
        when(response.headers()).thenReturn(mockHttpHeaders);

        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(response);

        ResponseEntity<String> actualResponse = executePostRequestAndReturn(new MockResponse());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_PROBLEM_DETAIL, actualResponse.getBody());

        verify(exceptionHandler, times(1)).handleHttpClientRequestFailureException(any(HttpClientResponseHeaderException.class), any(WebRequest.class));
    }

    @Test
    void writeServerReturns201_noLocationProvided_shouldReturn500() throws Exception {
        MockResponse mockResponse = new MockResponse().setResponseCode(201);
        ResponseEntity<String> actualResponse = executePostRequestAndReturn(mockResponse);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR_PROBLEM_DETAIL, actualResponse.getBody());

        verify(exceptionHandler, times(1)).handleHttpClientRequestFailureException(any(HttpClientResponseHeaderLocationException.class), any(WebRequest.class));
    }

    private ResponseEntity<String> executePostRequestAndReturn(MockResponse mockResponse) throws Exception {
        MockWebServer writeServer = new MockWebServer();

        writeServer.enqueue(mockResponse);

        // Overwrite base url defined in test/resources/application-test.yaml through reflection
        Field writeServerBaseUrl = httpClientService.getClass().getDeclaredField("WRITE_SERVER_BASE_URL");
        writeServerBaseUrl.setAccessible(true);
        writeServerBaseUrl.set(httpClientService, writeServer.url("").toString());

        // request to writeserver
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(REQUEST_BODY, headers);

        ResponseEntity<String> response = restTemplate.exchange(WEB_SERVER_PATH, HttpMethod.POST, httpEntity, String.class);

        // cleanup
        writeServer.close();

        return response;
    }
}

