package be.pbin.webserver.api;

import be.pbin.webserver.client.HttpClientRequestFailureException;
import be.pbin.webserver.client.HttpClientService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/note")
public class WebController {

    private final HttpClientService httpClientService;

    public WebController(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @GetMapping
    private ResponseEntity<String> get(@RequestParam(name = "shortlink") String shortLink) throws HttpClientRequestFailureException {
        return httpClientService.get(shortLink);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Void> create(@Valid @RequestBody Note note) throws HttpClientRequestFailureException {
        return httpClientService.post(note);
    }
}
