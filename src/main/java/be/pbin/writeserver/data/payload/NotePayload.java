package be.pbin.writeserver.data.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class NotePayload {

    private String id;
    private String payload;
}
