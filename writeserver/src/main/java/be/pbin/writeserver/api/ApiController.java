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

    @PostMapping(value = "/save",
            consumes = "application/json;charset=UTF-8",
            produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> createNoteData(@Valid @RequestBody NoteDTO noteDTO) throws DataProcessingException {
        URI uri = noteService.save(noteDTO);
        return ResponseEntity.created(uri).build();
    }
}
