package be.pbin.writeserver.service;

import be.pbin.writeserver.api.NoteData;
import be.pbin.writeserver.data.metadata.NoteMetaData;
import be.pbin.writeserver.data.payload.NotePayload;
import be.pbin.writeserver.data.payload.PayloadRepository;
import be.pbin.writeserver.data.metadata.NoteMetadataRepository;
import be.pbin.writeserver.utils.UriUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.net.URI;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplementationTest {

    @Mock
    private NoteMetadataRepository metadataRepository;
    @Mock
    private PayloadRepository payloadRepository;

    @InjectMocks
    private NoteServiceImplementation noteService;

    @Test
    void test_saveNote() {
        String payload = RandomStringUtils.randomAlphabetic(10);
        String path_to_payload = RandomStringUtils.randomAlphabetic(10);
        int expiration = Integer.parseInt(RandomStringUtils.randomNumeric(2));
        NoteData note = new NoteData(expiration, payload);

        doReturn(path_to_payload).when(payloadRepository).savePayload(any(NotePayload.class));

        URI result = noteService.save(note);
        String uniqueNoteId = UriUtils.extractLastSegment(result);

        ArgumentCaptor<NotePayload> blobCaptor = ArgumentCaptor.forClass(NotePayload.class);
        verify(payloadRepository).savePayload(blobCaptor.capture());

        NotePayload capturedPayload = blobCaptor.getValue();
        assertThat(capturedPayload.getId()).isEqualTo(uniqueNoteId);
        assertThat(capturedPayload.getPayload()).isEqualTo(payload);

        verify(metadataRepository).existsById(eq(uniqueNoteId));

        ArgumentCaptor<NoteMetaData> metaDataCaptor = ArgumentCaptor.forClass(NoteMetaData.class);
        verify(metadataRepository).save(metaDataCaptor.capture());

        NoteMetaData capturedMetaData = metaDataCaptor.getValue();
        assertThat(capturedMetaData.getPath()).isEqualTo(path_to_payload);
        assertThat(capturedMetaData.getExpirationTime()).isEqualTo(expiration);
        assertThat(capturedMetaData.getShortLink()).isEqualTo(uniqueNoteId);

        LocalDateTime justNow = LocalDateTime.now().minusSeconds(1);
        LocalDateTime now = LocalDateTime.now();
        assertThat(capturedMetaData.getCreationDate()).isBetween(justNow, now);
    }
}