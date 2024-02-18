package be.pbin.writeserver.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Objects;

@Getter
public class PasteData {

    @JsonProperty("expiration_time_in_minutes")
    private final int expirationTimeInMinutes;
    @JsonProperty("paste_contents")
    private final String pasteContents;

    public PasteData(int expirationTimeInMinutes, String pasteContents) {
        this.expirationTimeInMinutes = expirationTimeInMinutes;
        this.pasteContents = pasteContents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasteData pasteData = (PasteData) o;
        return expirationTimeInMinutes == pasteData.expirationTimeInMinutes && Objects.equals(pasteContents, pasteData.pasteContents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expirationTimeInMinutes, pasteContents);
    }
}
