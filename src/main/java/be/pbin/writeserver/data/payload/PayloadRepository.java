package be.pbin.writeserver.data.payload;

public interface PayloadRepository {

    /**
     * Returns the URI of the location where the given payload object is stored.
     */
    String savePayload(NotePayload objectStore);
}
