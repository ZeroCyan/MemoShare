package be.pbin.writeserver.service;

import be.pbin.writeserver.api.NoteData;
import be.pbin.writeserver.data.payload.NotePayload;
import be.pbin.writeserver.data.payload.PayloadRepository;
import be.pbin.writeserver.data.metadata.NoteMetaData;
import be.pbin.writeserver.data.metadata.NoteMetadataRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
@Service
public class NoteServiceImplementation implements NoteService {
    private final NoteMetadataRepository metadataRepository;
    private final PayloadRepository payloadRepository;

    public NoteServiceImplementation(NoteMetadataRepository metadataRepository, PayloadRepository payloadRepository) {
        this.metadataRepository = metadataRepository;
        this.payloadRepository = payloadRepository;
    }

    @Override
    public URI save(NoteData noteData) {
        String uniqueNoteId = generateNoteId();

        NotePayload payload = NotePayload.builder()
                .id(uniqueNoteId)
                .payload(noteData.getNoteContent())
                .build();

        String pathToObject = payloadRepository.savePayload(payload);

        NoteMetaData metadata = NoteMetaData.builder()
                .shortLink(uniqueNoteId)
                .path(pathToObject)
                .creationDate(LocalDateTime.now())
                .expirationTime(noteData.getExpirationTimeInMinutes()).build();

        metadataRepository.save(metadata);
        return URI.create("/api/get/" + uniqueNoteId);
    }

    private String generateNoteId() {
        String randomIdentifier = RandomStringUtils.random(8, true, true);

        while (metadataRepository.existsById(randomIdentifier)) {
            randomIdentifier = RandomStringUtils.random(8, true, true);
        }
        return randomIdentifier;
    }
}
