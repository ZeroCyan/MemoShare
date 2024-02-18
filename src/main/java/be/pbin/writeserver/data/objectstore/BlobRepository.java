package be.pbin.writeserver.data.objectstore;

public interface BlobRepository {

    /**
     * Returns the URI of the location where the given object is stored.
     */
    String saveObject(BlobModel objectStore);
}
