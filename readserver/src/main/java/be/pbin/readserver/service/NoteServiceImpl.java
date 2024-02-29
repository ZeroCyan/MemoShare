package be.pbin.readserver.service;

import be.pbin.readserver.api.NoteDTO;
import be.pbin.readserver.data.DataProcessingException;
import be.pbin.readserver.data.metadata.MetaData;
import be.pbin.readserver.data.metadata.MetaDataException;
import be.pbin.readserver.data.metadata.MetadataRepository;
import be.pbin.readserver.data.payload.PayloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NoteServiceImpl implements NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteServiceImpl.class);

    private final MetadataRepository metadataRepository;
    private final PayloadRepository payloadRepository;

    public NoteServiceImpl(MetadataRepository metadataRepository,
                           PayloadRepository payloadRepository) {
        this.metadataRepository = metadataRepository;
        this.payloadRepository = payloadRepository;
    }

    @Override
    public Optional<NoteDTO> get(String id) throws DataProcessingException {
        Optional<MetaData> optionalMetaData;
        try {
            optionalMetaData = metadataRepository.findById(id);
        } catch (DataAccessException exception) {
            log.error("Error during retrieval of metadata of note with id {}: {}", id, exception.getMessage(), exception);
            throw new MetaDataException("Error during saving of metadata of note with id: " + id, exception);
        }

        MetaData metaData;

        if (optionalMetaData.isPresent() && hasNotExpired(optionalMetaData.get())) {
            metaData = optionalMetaData.get();
        } else {
            log.info("Unable to locate metadata for id {}", id);
            return Optional.empty();
        }

        String payload = payloadRepository.get(id); // todo: logic is too spread here. Some data exceptions in service, some in repository.

        return payload.isEmpty() ? Optional.empty() : Optional.of(new NoteDTO(payload, metaData.getCreationDate()));
    }

    private boolean hasNotExpired(MetaData metaData) {
        return metaData.getExpirationDate().isAfter(LocalDateTime.now());
    }
}
