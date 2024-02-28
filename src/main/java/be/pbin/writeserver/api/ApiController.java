package be.pbin.writeserver.api;

import be.pbin.writeserver.data.DataProcessingException;
import be.pbin.writeserver.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final NoteService noteService;

    public ApiController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/get/{identifier}") //todo: temporary get method, later will move to READ SERVER.
    private ResponseEntity<String> getData(@PathVariable String identifier) {
        return ResponseEntity.ok().header("forId", identifier).build();
    }

    @PostMapping(value = "/save",
            consumes = "application/json;charset=UTF-8",
            produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> createNoteData(@Valid @RequestBody NoteDTO noteDTO) throws DataProcessingException {
        URI uri = noteService.save(noteDTO);
        return ResponseEntity.created(uri).build();
    }
}
