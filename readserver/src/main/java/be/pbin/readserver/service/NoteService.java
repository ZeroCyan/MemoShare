package be.pbin.readserver.service;

import be.pbin.readserver.api.NoteDTO;
import be.pbin.readserver.data.DataProcessingException;

import java.util.Optional;

public interface NoteService {

    Optional<NoteDTO> get(String id) throws DataProcessingException;
}
