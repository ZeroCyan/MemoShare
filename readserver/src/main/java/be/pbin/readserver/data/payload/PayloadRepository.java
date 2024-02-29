package be.pbin.readserver.data.payload;

public interface PayloadRepository {

    String get(String payloadId) throws PayloadAccessException;

}
