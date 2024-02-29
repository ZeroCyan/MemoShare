package be.pbin.writeserver.service;

import be.pbin.writeserver.api.NoteDTO;
import be.pbin.writeserver.data.DataProcessingException;
import be.pbin.writeserver.data.metadata.MetaData;
import be.pbin.writeserver.data.metadata.MetaDataException;
import be.pbin.writeserver.data.MetadataRepository;
import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.PayloadRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
public class NoteServiceImpl implements NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteServiceImpl.class);

    @Value("${be.pbin.web-server.endpoint}")
    private String WEB_SERVER_ENDPOINT;

    private final MetadataRepository metadataRepository;
    private final PayloadRepository payloadRepository;


    public NoteServiceImpl(MetadataRepository metadataRepository,
                           PayloadRepository payloadRepository) {
        this.metadataRepository = metadataRepository;
        this.payloadRepository = payloadRepository;
    }

    @Override
    public URI save(NoteDTO noteDTO) throws DataProcessingException {
        String uniqueNoteId = generateUniqueIdentifier();

        Payload payload = Payload.builder()
                .id(uniqueNoteId)
                .payload(noteDTO.getNoteContent())
                .build();

        String pathToPayload = payloadRepository.save(payload);

        MetaData metadata = MetaData.builder()
                .shortLink(uniqueNoteId)
                .path(pathToPayload)
                .creationDate(LocalDateTime.now())
                .expirationDate(calculateExpiration(noteDTO))
                .build();

        try {
            metadataRepository.save(metadata);
        } catch (DataAccessException exception) {
            log.error("Error during saving of metadata of note with id {}: {}", uniqueNoteId, exception.getMessage(), exception);
            payloadRepository.deleteById(uniqueNoteId);
            throw new MetaDataException("Error during saving of metadata of note with id: " + uniqueNoteId, exception);
        }
        return URI.create(WEB_SERVER_ENDPOINT + "/" +uniqueNoteId);
    }

    private LocalDateTime calculateExpiration(NoteDTO noteDTO) {
        if (noteDTO.getExpirationTimeInMinutes() == 0) {
            return LocalDateTime.of(9999, 12, 31, 0, 0, 0);
        }
        return LocalDateTime.now().plusMinutes(noteDTO.getExpirationTimeInMinutes());
    }

    private String generateUniqueIdentifier() { //NiceToHave: generate ID from more refined method
        String randomIdentifier = RandomStringUtils.randomAlphanumeric(8);

        while (metadataRepository.existsById(randomIdentifier)) {
            randomIdentifier = RandomStringUtils.randomAlphanumeric(8);
        }
        return randomIdentifier;
    }
}
