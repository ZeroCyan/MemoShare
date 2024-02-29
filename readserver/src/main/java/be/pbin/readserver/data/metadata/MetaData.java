package be.pbin.readserver.data.metadata;

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
public class MetaData {

    @Id
    @Column(name = "short_link")
    private String shortLink;

    @Column(name = "path_to_note")
    private String path;

    @Column(name = "created_at")
    private LocalDateTime creationDate;

    @Column(name = "expires_at")
    private LocalDateTime expirationDate;
}
