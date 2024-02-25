package be.pbin.writeserver.data.payload.azurite;

import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.PayloadStorageException;
import be.pbin.writeserver.data.payload.PayloadRepository;
import com.azure.core.exception.AzureException;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.lang3.ObjectUtils.allNotNull;

@Component
public class AzuritePayloadRepository implements PayloadRepository {

    private static final Logger log = LoggerFactory.getLogger(AzuritePayloadRepository.class);

    @Override
    public String save(Payload payload) throws PayloadStorageException {
        allNotNull(payload, payload.payload(), payload.id());

        BlobClient blobClient = getBlobClient(payload.id());

        if (payload.payload() != null) {
            try (InputStream inputStream = new ByteArrayInputStream(payload.payload().getBytes())){
                blobClient.upload(inputStream);
            } catch (BlobStorageException | IOException exception) {
                //Question: unsure if this is good practice. The idea is to handle the IOException where it occurs,
                // and let GlobalExceptionHandler.handleGeneralException() handle the response to the client.
                throw new PayloadStorageException("Error occurred during payload upload to Azure storage.", exception);
            }
        }
        return blobClient.getBlobUrl();
    }

    @Override
    public void deleteById(String payloadId) {
        BlobClient blobClient = getBlobClient(payloadId);
        try {
            blobClient.delete();
        } catch (BlobStorageException exception) {
            log.error("Error deleting blob in azure storage: Error code={}, Message={}", exception.getErrorCode(), exception.getMessage(), exception);
        } catch (AzureException exception) {
            log.error("Unexpected error deleting blob in azure storage. Message= {}",exception.getMessage(), exception);
        }
    }

    /**
     * A {@link BlobContainerClient} represents the container where blobs are stored.
     * A {@link BlobClient} represents an individual blob.
     */
    private BlobClient getBlobClient(String payloadId) {
        BlobContainerClient containerClient = ContainerClientFactory.getContainerClient();
        containerClient.createIfNotExists();
        return containerClient.getBlobClient(payloadId);
    }
}
