package be.pbin.writeserver.data.sql;

import org.springframework.data.repository.CrudRepository;

public interface SQLRepository extends CrudRepository<NoteModel, String> {
}
