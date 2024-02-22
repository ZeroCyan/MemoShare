package be.pbin.writeserver.data.metadata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "notes")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteMetaData {

    @Id
    @Column(name = "short_link")
    private String shortLink;

    @Column(name = "path_to_note")
    private String path;

    @Column(name = "created_at")
    private LocalDateTime creationDate;

    @Column(name = "expiration_length_in_minutes")
    private int expirationTime;
}
