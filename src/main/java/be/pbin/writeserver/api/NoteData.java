package be.pbin.writeserver.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.Objects;

@Getter
public class NoteData {

    @JsonProperty("expiration_time_in_minutes") //todo: add JSON validation
    @Min(value = 0L, message = "Expiration time cannot be negative")
    @Max(value = 26_000_000, message = "Expiration time exceeds limit. Hint: Set expiration to 0 to prevent expiration.")
    private int expirationTimeInMinutes;

    @JsonProperty(value = "note_contents")
    @NotNull(message = "'note_contents' must be present in the request. Hint: check spelling")
    @NotEmpty(message = "'note_contents' is empty")
    @Size(max = 1_000_000, message = "Character limit exceeded. The maximum allowed is 1 million characters.")
    private final String noteContent;

    public NoteData(int expirationTimeInMinutes, String noteContent) {
        this.expirationTimeInMinutes = expirationTimeInMinutes;
        this.noteContent = noteContent;
    }

    public NoteData(String noteContent) {
        this.noteContent = noteContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteData noteData = (NoteData) o;
        return expirationTimeInMinutes == noteData.expirationTimeInMinutes && Objects.equals(noteContent, noteData.noteContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expirationTimeInMinutes, noteContent);
    }
}
