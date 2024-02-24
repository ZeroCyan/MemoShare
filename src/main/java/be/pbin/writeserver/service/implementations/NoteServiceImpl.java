package be.pbin.writeserver.service.implementations;

import be.pbin.writeserver.api.NoteData;
import be.pbin.writeserver.data.metadata.NoteMetaData;
import be.pbin.writeserver.data.metadata.NoteMetadataRepository;
import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.PayloadRepository;
import be.pbin.writeserver.data.payload.validation.InvalidPayloadException;
import be.pbin.writeserver.service.NoteService;
import be.pbin.writeserver.service.ValidationService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMetadataRepository metadataRepository;
    private final PayloadRepository payloadRepository;
    private final ValidationService validationService;

    public NoteServiceImpl(NoteMetadataRepository metadataRepository,
                           PayloadRepository payloadRepository,
                           ValidationService validationService) {
        this.metadataRepository = metadataRepository;
        this.payloadRepository = payloadRepository;
        this.validationService = validationService;
    }

    @Override
    public URI save(NoteData noteData) throws InvalidPayloadException {
        String uniqueNoteId = generateUniqueIdentifier();

        Payload payload = Payload.builder()
                .id(uniqueNoteId)
                .payload(noteData.getNoteContent())
                .build();

        validationService.validate(payload);

        String pathToPayload = payloadRepository.savePayload(payload);

        NoteMetaData metadata = NoteMetaData.builder()
                .shortLink(uniqueNoteId)
                .path(pathToPayload)
                .creationDate(LocalDateTime.now())
                .expirationTime(noteData.getExpirationTimeInMinutes()).build();

        metadataRepository.save(metadata);
        return URI.create("/api/get/" + uniqueNoteId);
    }

    private String generateUniqueIdentifier() { //todo: generate ID from more refined method
        String randomIdentifier = RandomStringUtils.randomAlphanumeric(8);

        while (metadataRepository.existsById(randomIdentifier)) {
            randomIdentifier = RandomStringUtils.randomAlphanumeric(8);
        }
        return randomIdentifier;
    }
}
