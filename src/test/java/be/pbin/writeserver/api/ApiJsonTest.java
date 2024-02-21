package be.pbin.writeserver.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ApiJsonTest {

    @Autowired
    private JacksonTester<PasteData> json;

    @Test
    void pasteDataSerializationTest() throws IOException {
        PasteData pasteData = new PasteData(60, "dummy paste content");

        assertThat(json.write(pasteData)).isStrictlyEqualToJson("expected.json");

        assertThat(json.write(pasteData)).hasJsonPathNumberValue("@.expiration_time_in_minutes");
        assertThat(json.write(pasteData))
                .extractingJsonPathNumberValue("@.expiration_time_in_minutes")
                .isEqualTo(60);

        assertThat(json.write(pasteData)).hasJsonPathStringValue("@.paste_contents");
        assertThat(json.write(pasteData))
                .extractingJsonPathStringValue("@.paste_contents")
                .isEqualTo("dummy paste content");
    }

    @Test
    void pasteDataDeserializationTest() throws IOException {
        String expected = """
                {
                  "expiration_time_in_minutes": 60,
                  "paste_contents": "dummy paste content"
                }
                """;
        assertThat(json.parse(expected)).isEqualTo(new PasteData(60, "dummy paste content"));
        assertThat(json.parseObject(expected).getExpirationTimeInMinutes()).isEqualTo(60);
        assertThat(json.parseObject(expected).getPasteContent()).isEqualTo("dummy paste content");
    }
}
