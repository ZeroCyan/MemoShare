package be.pbin.readserver.data.payload.azurite;

import be.pbin.readserver.data.payload.PayloadAccessException;
import be.pbin.readserver.data.payload.PayloadRepository;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class AzuritePayloadRepository implements PayloadRepository {

    private static final Logger log = LoggerFactory.getLogger(AzuritePayloadRepository.class);

    @Override
    public String get(String payloadId) throws PayloadAccessException {
        BlobClient blobClient = getBlobClient(payloadId);
        try {
            BinaryData binaryData = blobClient.downloadContent();

            if (binaryData != null) {
                ByteBuffer byteBuffer = binaryData.toByteBuffer();
                return StandardCharsets.UTF_8.decode(byteBuffer).toString();
            } else {
                throw new IOException("Content download unsuccessful");
            }
        } catch (IOException exception) {
            log.error("Error during retrieval of blob from azure storage for id {}. Error message: {}", payloadId, exception.getCause(), exception);
            throw new PayloadAccessException("Could not retrieve blob from azure storage");
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
