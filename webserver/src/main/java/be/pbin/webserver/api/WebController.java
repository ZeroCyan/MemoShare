package be.pbin.webserver.api;

import be.pbin.webserver.client.HttpClientRequestFailureException;
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> get(@Valid @Nonnull
                                       @Pattern(regexp = "^[a-zA-Z0-9]{8}$", message = "The request parameter must be 8 characters long and consist of alphanumeric characters.")
                                       @RequestParam(name = "shortlink") String shortLink) throws HttpClientRequestFailureException {
        return httpClientService.get(shortLink);
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Void> create(@Valid @RequestBody Note note) throws HttpClientRequestFailureException {
        return httpClientService.post(note);
    }
}
