package be.pbin.webserver.api;

import be.pbin.webserver.client.exceptions.HttpClientException;
import be.pbin.webserver.client.HttpClientService;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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

    @GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
    private ResponseEntity<String> get(@Valid @Nonnull
                                       @Pattern(regexp = "^[a-zA-Z0-9]{8}$", message = "The request parameter must be 8 characters long and consist of alphanumeric characters.")
                                       @PathVariable(name = "id") String id) throws HttpClientException {
        return httpClientService.get(id);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> create(@Valid @RequestBody Note note) throws HttpClientException {
        return httpClientService.post(note);
    }
}
