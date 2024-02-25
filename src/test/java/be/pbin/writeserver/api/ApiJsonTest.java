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
    private JacksonTester<NoteDTO> json;

    @Test
    void noteDataSerializationTest() throws IOException {
        NoteDTO noteDTO = new NoteDTO(60, "dummy note content");

        assertThat(json.write(noteDTO)).isStrictlyEqualToJson("expected.json");

        assertThat(json.write(noteDTO)).hasJsonPathNumberValue("@.expiration_time_in_minutes");
        assertThat(json.write(noteDTO))
                .extractingJsonPathNumberValue("@.expiration_time_in_minutes")
                .isEqualTo(60);

        assertThat(json.write(noteDTO)).hasJsonPathStringValue("@.note_contents");
        assertThat(json.write(noteDTO))
                .extractingJsonPathStringValue("@.note_contents")
                .isEqualTo("dummy note content");
    }

    @Test
    void noteDataDeserializationTest() throws IOException {
        String expected = """
                {
                  "expiration_time_in_minutes": 60,
                  "note_contents": "dummy note content"
                }
                """;
        assertThat(json.parse(expected)).isEqualTo(new NoteDTO(60, "dummy note content"));
        assertThat(json.parseObject(expected).getExpirationTimeInMinutes()).isEqualTo(60);
        assertThat(json.parseObject(expected).getNoteContent()).isEqualTo("dummy note content");
    }
}
