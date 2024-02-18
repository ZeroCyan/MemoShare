package be.pbin.writeserver.data.objectstore.azurite;

import be.pbin.writeserver.data.objectstore.BlobModel;
import be.pbin.writeserver.data.objectstore.BlobRepository;
import com.azure.storage.blob.*;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class AzuriteRepository implements BlobRepository {

    private static final String BLOB_SERVICE_URL = "http://localhost:4242/";
    private static final String BLOB_CONTAINER_NAME = "pastes";
    private static final String AZURITE_DEFAULT_ACCOUNT = "devstoreaccount1";
    private static final String AZURITE_DEFAULT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    private static final String AZURITE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=" + BLOB_SERVICE_URL + AZURITE_DEFAULT_ACCOUNT;

    @Override
    public String saveObject(BlobModel blob) {
        StorageSharedKeyCredential credentials =  new StorageSharedKeyCredential(AZURITE_DEFAULT_ACCOUNT, AZURITE_DEFAULT_KEY);

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(AZURITE_CONNECTION_STRING)
                .credential(credentials)
                .containerName(BLOB_CONTAINER_NAME)
                .buildClient();

        containerClient.createIfNotExists();
        BlobClient blobClient = containerClient.getBlobClient(blob.getId());

        InputStream inputStream = new ByteArrayInputStream(blob.getPayload().getBytes()); //todo null checks
        blobClient.upload(inputStream);

        return blobClient.getBlobUrl();
    }
}
