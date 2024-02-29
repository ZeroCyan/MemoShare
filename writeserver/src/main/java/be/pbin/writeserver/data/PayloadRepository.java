package be.pbin.writeserver.data;

import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.PayloadStorageException;

public interface PayloadRepository {

    /**
     * Returns the URI of the location where the given payload object is stored.
     */
    String save(Payload objectStore) throws PayloadStorageException;

    void deleteById(String payloadId);
}
