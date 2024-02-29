package be.pbin.readserver.api;

import be.pbin.readserver.data.DataProcessingException;
import be.pbin.readserver.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final NoteService noteService;

    public ApiController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping(value = "/get/{id}")
    private ResponseEntity<NoteDTO> getNoteData(@PathVariable String id) throws DataProcessingException {
        Optional<NoteDTO> note = noteService.get(id);
        return ResponseEntity.of(note);
    }
}
