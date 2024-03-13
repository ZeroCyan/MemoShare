package be.pbin.webserver.client;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
public class HttpClientFactory {

    private HttpClient httpClient;

    //todo: consider whether reusing the same client for all requests is a good idea
    public HttpClient getHttpClient() {
        if (this.httpClient == null) {
            this.httpClient = HttpClient.newHttpClient();
        }
        return this.httpClient;
    }
}
