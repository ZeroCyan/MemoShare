package be.pbin.readserver.api;

import be.pbin.readserver.data.DataProcessingException;
import be.pbin.readserver.service.NoteService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/note")
public class ReadController {

    private final NoteService noteService;

    public ReadController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE})
    private ResponseEntity<NoteDTO> get(@PathVariable String id) throws DataProcessingException {
        Optional<NoteDTO> note = noteService.get(id);
        return ResponseEntity.of(note);
    }
}
