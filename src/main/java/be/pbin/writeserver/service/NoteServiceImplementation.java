package be.pbin.writeserver.service;

import be.pbin.writeserver.api.NoteData;
import be.pbin.writeserver.data.objectstore.BlobModel;
import be.pbin.writeserver.data.objectstore.BlobRepository;
import be.pbin.writeserver.data.sql.NoteModel;
import be.pbin.writeserver.data.sql.SQLRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
@Service
public class NoteServiceImplementation implements NoteService {
    private final SQLRepository sqlRepository;
    private final BlobRepository blobRepository;

    public NoteServiceImplementation(SQLRepository sqlRepository, BlobRepository blobRepository) {
        this.sqlRepository = sqlRepository;
        this.blobRepository = blobRepository;
    }

    @Override
    public URI save(NoteData noteData) {
        String noteId = getNoteId();

        BlobModel blob = new BlobModel();
        blob.setId(noteId);
        blob.setPayload(noteData.getNoteContent());

        String pathToObject = blobRepository.saveObject(blob);

        NoteModel newNote = NoteModel.builder()
                .shortLink(noteId)
                .path(pathToObject)
                .creationDate(LocalDateTime.now())
                .expirationTime(noteData.getExpirationTimeInMinutes()).build();

        sqlRepository.save(newNote);
        return URI.create("/api/get/" + noteId);
    }

    private String getNoteId() {
        String randomIdentifier = RandomStringUtils.random(8, true, true);

        while (sqlRepository.existsById(randomIdentifier)) {
            randomIdentifier = RandomStringUtils.random(8, true, true);
        }
        return randomIdentifier;
    }
}
