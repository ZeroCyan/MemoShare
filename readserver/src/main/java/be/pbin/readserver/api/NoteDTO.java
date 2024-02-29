package be.pbin.readserver.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record NoteDTO(@JsonProperty(value = "note_contents") String payload,
                      @JsonProperty(value = "created_at") LocalDateTime createdAt) {
}
