package be.pbin.webserver.client;

import be.pbin.webserver.api.Note;
import org.springframework.http.ResponseEntity;

public interface HttpClientService {

    ResponseEntity<String> get(String shortlink) throws HttpClientRequestFailureException;

    ResponseEntity<Void> post(Note note) throws HttpClientRequestFailureException;
}
