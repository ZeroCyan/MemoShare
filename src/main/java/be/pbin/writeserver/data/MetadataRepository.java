package be.pbin.writeserver.data;

import be.pbin.writeserver.data.metadata.MetaData;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MetadataRepository extends CrudRepository<MetaData, String> {

    List<MetaData> findByExpirationDateBefore(LocalDateTime dateTime); //NiceToHave: Paged result
}
