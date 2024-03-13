package be.pbin.webserver.client;

import be.pbin.webserver.api.Note;
import be.pbin.webserver.client.exceptions.HttpClientException;
import org.springframework.http.ResponseEntity;

public interface HttpClientService {

    ResponseEntity<String> get(String shortlink) throws HttpClientException;

    ResponseEntity<String> post(Note note) throws HttpClientException;
}
