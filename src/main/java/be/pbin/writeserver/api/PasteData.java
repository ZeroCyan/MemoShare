package be.pbin.writeserver.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.Objects;

@Getter
public class PasteData {

    @JsonProperty("expiration_time_in_minutes") //todo: add JSON validation
    @Min(value = 0L, message = "Expiration time cannot be negative")
    @Max(value = 26_000_000, message = "Expiration time exceeds limit. Hint: Set expiration to 0 to prevent expiration.")
    private int expirationTimeInMinutes;

    @JsonProperty(value = "paste_contents")
    @NotNull(message = "'paste_contents' must be present in the request. Hint: check spelling")
    @NotEmpty(message = "'paste_contents' is empty")
    @Size(max = 1_000_000, message = "Character limit exceeded. The maximum allowed is 1 million characters.")
    private final String pasteContent;

    public PasteData(int expirationTimeInMinutes, String pasteContent) {
        this.expirationTimeInMinutes = expirationTimeInMinutes;
        this.pasteContent = pasteContent;
    }

    public PasteData (String pasteContent) {
        this.pasteContent = pasteContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasteData pasteData = (PasteData) o;
        return expirationTimeInMinutes == pasteData.expirationTimeInMinutes && Objects.equals(pasteContent, pasteData.pasteContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expirationTimeInMinutes, pasteContent);
    }
}
