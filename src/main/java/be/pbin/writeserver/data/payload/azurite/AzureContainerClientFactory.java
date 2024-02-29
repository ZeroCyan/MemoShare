package be.pbin.writeserver.data.payload.azurite;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AzureContainerClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(AzureContainerClientFactory.class);

    @Getter
    private static BlobContainerClient containerClient;

    @Value("${azurite.service.url}")
    private String payloadServiceUrl;

    @Value("${azurite.service.container}")
    private String payloadContainerName;

    @Value("${azurite.service.account}")
    private String azuriteDefaultAccount;

    @Value("${azurite.service.key}")
    private String azuriteDefaultKey;

    @Value("${azurite.service.connection}")
    private String azuriteConnectionString;

    @PostConstruct
    private void initialize() {
        StorageSharedKeyCredential credentials = new StorageSharedKeyCredential(azuriteDefaultAccount, azuriteDefaultKey);

        logger.info("Instantiating connection client for azurite blob container: " + payloadContainerName);

        containerClient = new BlobContainerClientBuilder()
                .connectionString(azuriteConnectionString + payloadServiceUrl + azuriteDefaultAccount)
                .credential(credentials)
                .containerName(payloadContainerName)
                .buildClient();
    }
}
