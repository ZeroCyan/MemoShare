package be.pbin.writeserver.api;

import be.pbin.writeserver.service.PasteService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final PasteService pasteService;

    public ApiController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @GetMapping("/get/{identifier}") //todo: temporary get method, later will move to READ SERVER.
    private ResponseEntity<String> getData(@PathVariable String identifier) {
        return ResponseEntity.ok().header("forId", identifier).build();
    }

    @PostMapping(value = "/paste",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> pasteData(@RequestBody PasteData pasteData) { //todo: validate request body
        URI uri = pasteService.save(pasteData);
        return ResponseEntity.created(uri).build();
    }
}
