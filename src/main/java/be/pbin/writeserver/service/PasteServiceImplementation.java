package be.pbin.writeserver.service;

import be.pbin.writeserver.api.PasteData;
import be.pbin.writeserver.data.objectstore.ObjectStoreModel;
import be.pbin.writeserver.data.objectstore.ObjectStoreRepository;
import be.pbin.writeserver.data.sql.PasteModel;
import be.pbin.writeserver.data.sql.SQLRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
public class PasteServiceImplementation implements PasteService {

    private final SQLRepository sqlRepository;
    private final ObjectStoreRepository objectStoreRepository;

    public PasteServiceImplementation(SQLRepository sqlRepository, ObjectStoreRepository objectStoreRepository) {
        this.sqlRepository = sqlRepository;
        this.objectStoreRepository = objectStoreRepository;
    }

    @Override
    public URI save(PasteData pasteData) {
        String randomIdentifier = RandomStringUtils.random(8, true, true);

        while (sqlRepository.existsById(randomIdentifier)) {
            randomIdentifier = RandomStringUtils.random(8, true, true);
        }

        ObjectStoreModel objectStore = new ObjectStoreModel();
        objectStore.setId(randomIdentifier);
        objectStore.setPayload(pasteData.getPasteContents());

        URI pathToObject = objectStoreRepository.saveObject(objectStore);

        PasteModel newPaste = new PasteModel();
        newPaste.setShortLink(randomIdentifier);
        newPaste.setCreationDate(LocalDateTime.now());
        newPaste.setExpirationTime(pasteData.getExpirationTimeInMinutes());
        newPaste.setPath(pathToObject.toString());
        sqlRepository.save(newPaste);

        return URI.create("/api/get/" + randomIdentifier);
    }
}
