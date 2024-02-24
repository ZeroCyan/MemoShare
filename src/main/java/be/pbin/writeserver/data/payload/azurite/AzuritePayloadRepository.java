package be.pbin.writeserver.data.payload.azurite;

import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.PayloadRepository;
import com.azure.storage.blob.*;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class AzuritePayloadRepository implements PayloadRepository {

    @Value("${azurite.service.url}")
    private String PAYLOAD_SERVICE_URL;
    @Value("${azurite.service.container}")
    private String PAYLOAD_CONTAINER_NAME;
    @Value("${azurite.service.account}")
    private String AZURITE_DEFAULT_ACCOUNT;
    @Value("${azurite.service.key}")
    private String AZURITE_DEFAULT_KEY;
    @Value("${azurite.service.connection}")
    private String AZURITE_CONNECTION_STRING;

    @Override
    public String savePayload(Payload payload) {
        BlobClient blobClient = getBlobClient(payload);

        InputStream inputStream = new ByteArrayInputStream(payload.payload().getBytes()); //todo null checks

        blobClient.upload(inputStream);

        return blobClient.getBlobUrl();
    }

    private BlobClient getBlobClient(Payload payload) {
        StorageSharedKeyCredential credentials =  new StorageSharedKeyCredential(AZURITE_DEFAULT_ACCOUNT, AZURITE_DEFAULT_KEY);

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(AZURITE_CONNECTION_STRING + PAYLOAD_SERVICE_URL + AZURITE_DEFAULT_ACCOUNT)
                .credential(credentials)
                .containerName(PAYLOAD_CONTAINER_NAME)
                .buildClient();

        containerClient.createIfNotExists();
        return containerClient.getBlobClient(payload.id());
    }
}
