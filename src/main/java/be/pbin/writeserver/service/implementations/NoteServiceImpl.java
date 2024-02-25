package be.pbin.writeserver.service.implementations;

import be.pbin.writeserver.api.NoteDTO;
import be.pbin.writeserver.data.metadata.MetaData;
import be.pbin.writeserver.data.metadata.MetadataRepository;
import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.PayloadRepository;
import be.pbin.writeserver.data.payload.PayloadStorageException;
import be.pbin.writeserver.data.payload.validation.InvalidPayloadException;
import be.pbin.writeserver.service.NoteService;
import be.pbin.writeserver.service.ValidationService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
public class NoteServiceImpl implements NoteService {

    private final MetadataRepository metadataRepository;
    private final PayloadRepository payloadRepository;
    private final ValidationService validationService;

    public NoteServiceImpl(MetadataRepository metadataRepository,
                           PayloadRepository payloadRepository,
                           ValidationService validationService) {
        this.metadataRepository = metadataRepository;
        this.payloadRepository = payloadRepository;
        this.validationService = validationService;
    }

    @Override
    public URI save(NoteDTO noteDTO) throws InvalidPayloadException, PayloadStorageException {
        String uniqueNoteId = generateUniqueIdentifier();

        Payload payload = Payload.builder()
                .id(uniqueNoteId)
                .payload(noteDTO.getNoteContent())
                .build();

        validationService.validate(payload);

        String pathToPayload = payloadRepository.save(payload);

        MetaData metadata = MetaData.builder()
                .shortLink(uniqueNoteId)
                .path(pathToPayload)
                .creationDate(LocalDateTime.now())
                .expirationDate(calculateExpiration(noteDTO))
                .build();

        metadataRepository.save(metadata); //todo: if this save method fails, remove the blob from the payload repository
        return URI.create("/api/get/" + uniqueNoteId);
    }

    private LocalDateTime calculateExpiration(NoteDTO noteDTO) {
        if (noteDTO.getExpirationTimeInMinutes() == 0) {
            return LocalDateTime.of(9999, 12, 31, 0, 0, 0);
        }
        return LocalDateTime.now().plusMinutes(noteDTO.getExpirationTimeInMinutes());
    }

    private String generateUniqueIdentifier() { //todo: generate ID from more refined method
        String randomIdentifier = RandomStringUtils.randomAlphanumeric(8);

        while (metadataRepository.existsById(randomIdentifier)) {
            randomIdentifier = RandomStringUtils.randomAlphanumeric(8);
        }
        return randomIdentifier;
    }
}
