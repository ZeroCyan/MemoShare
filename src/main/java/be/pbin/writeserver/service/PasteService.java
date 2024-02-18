package be.pbin.writeserver.service;

import be.pbin.writeserver.api.PasteData;

import java.net.URI;

public interface PasteService {

    /**
     * Saves the given {@link PasteData} instance. Returns the URI where the resource can be retrieved.
     */
    URI save(PasteData pasteData);
}
