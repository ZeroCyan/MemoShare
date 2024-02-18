package be.pbin.writeserver.service;

import be.pbin.writeserver.api.PasteData;
import be.pbin.writeserver.data.objectstore.BlobModel;
import be.pbin.writeserver.data.objectstore.BlobRepository;
import be.pbin.writeserver.data.sql.PasteModel;
import be.pbin.writeserver.data.sql.SQLRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
public class PasteServiceImplementation implements PasteService {

    private final SQLRepository sqlRepository;
    private final BlobRepository blobRepository;

    public PasteServiceImplementation(SQLRepository sqlRepository, BlobRepository blobRepository) {
        this.sqlRepository = sqlRepository;
        this.blobRepository = blobRepository;
    }

    @Override
    public URI save(PasteData pasteData) {
        String randomIdentifier = RandomStringUtils.random(8, true, true);

        while (sqlRepository.existsById(randomIdentifier)) {
            randomIdentifier = RandomStringUtils.random(8, true, true);
        }

        BlobModel blob = new BlobModel();
        blob.setId(randomIdentifier);
        blob.setPayload(pasteData.getPasteContents());

        String pathToObject = blobRepository.saveObject(blob);

        PasteModel newPaste = new PasteModel();
        newPaste.setShortLink(randomIdentifier);
        newPaste.setCreationDate(LocalDateTime.now());
        newPaste.setExpirationTime(pasteData.getExpirationTimeInMinutes());
        newPaste.setPath(pathToObject);
        sqlRepository.save(newPaste);

        return URI.create("/api/get/" + randomIdentifier);
    }
}
