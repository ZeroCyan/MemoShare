package be.pbin.writeserver.data.objectstore;

import java.net.URI;

public interface ObjectStoreRepository {

    /**
     * Returns the URI of the location where the given object is stored.
     */
    URI saveObject(ObjectStoreModel objectStore);
}
