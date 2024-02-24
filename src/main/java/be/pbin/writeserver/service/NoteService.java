package be.pbin.writeserver.service;

import be.pbin.writeserver.api.NoteData;
import be.pbin.writeserver.data.payload.validation.InvalidPayloadException;

import java.net.URI;

public interface NoteService {

    /**
     * Saves the given {@link NoteData} instance. Returns the URI where the resource can be retrieved.
     */
    URI save(NoteData noteData) throws InvalidPayloadException;
}
