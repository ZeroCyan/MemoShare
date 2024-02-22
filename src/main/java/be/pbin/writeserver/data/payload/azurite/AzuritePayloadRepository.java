package be.pbin.writeserver.data.payload.azurite;

import be.pbin.writeserver.data.payload.NotePayload;
import be.pbin.writeserver.data.payload.PayloadRepository;
import com.azure.storage.blob.*;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class AzuritePayloadRepository implements PayloadRepository {

    private static final String PAYLOAD_SERVICE_URL = "http://localhost:4242/";
    private static final String PAYLOAD_CONTAINER_NAME = "notes";
    private static final String AZURITE_DEFAULT_ACCOUNT = "devstoreaccount1";
    private static final String AZURITE_DEFAULT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    private static final String AZURITE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=" + PAYLOAD_SERVICE_URL + AZURITE_DEFAULT_ACCOUNT;

    @Override
    public String savePayload(NotePayload payload) {
        StorageSharedKeyCredential credentials =  new StorageSharedKeyCredential(AZURITE_DEFAULT_ACCOUNT, AZURITE_DEFAULT_KEY);

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(AZURITE_CONNECTION_STRING)
                .credential(credentials)
                .containerName(PAYLOAD_CONTAINER_NAME)
                .buildClient();

        containerClient.createIfNotExists();
        BlobClient blobClient = containerClient.getBlobClient(payload.getId());

        InputStream inputStream = new ByteArrayInputStream(payload.getPayload().getBytes()); //todo null checks
        blobClient.upload(inputStream);

        return blobClient.getBlobUrl();
    }
}
