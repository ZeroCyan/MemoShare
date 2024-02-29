package be.pbin.writeserver.service;

import be.pbin.writeserver.api.NoteDTO;
import be.pbin.writeserver.data.DataProcessingException;

import java.net.URI;

public interface NoteService {

    /**
     * Saves the given {@link NoteDTO} instance. Returns the URI where the resource can be retrieved.
     */
    URI save(NoteDTO noteDTO) throws DataProcessingException;
}
