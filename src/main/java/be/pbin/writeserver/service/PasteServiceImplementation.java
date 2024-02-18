package be.pbin.writeserver.service;

import be.pbin.writeserver.api.PasteData;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class PasteServiceImplementation implements PasteService{

    @Override
    public URI save(PasteData pasteData) {
        return URI.create("/get/123"); //todo: mock implementation
    }
}
