package be.pbin.writeserver.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Objects;

@Getter
public class PasteData {

    @JsonProperty("expiration_time_in_minutes") //todo: add JSON validation
    @Min(value = 0L, message = "Expiration time cannot be negative")
    private int expirationTimeInMinutes;
    @JsonProperty(value = "paste_contents")
    @NotNull(message = "'paste_contents' must be present in the request. Hint: check spelling")
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
