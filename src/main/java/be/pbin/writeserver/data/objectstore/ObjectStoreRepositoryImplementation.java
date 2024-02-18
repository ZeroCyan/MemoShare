package be.pbin.writeserver.data.objectstore;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class ObjectStoreRepositoryImplementation implements ObjectStoreRepository{
    @Override
    public URI saveObject(ObjectStoreModel objectStore) {
        return URI.create("/some/random/path/" + objectStore.getId());
    }
}
