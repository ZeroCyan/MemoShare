package be.pbin.writeserver.service;

import be.pbin.writeserver.api.NoteData;
import be.pbin.writeserver.data.metadata.MetadataRepository;
import be.pbin.writeserver.data.metadata.NoteMetaData;
import be.pbin.writeserver.data.payload.Payload;
import be.pbin.writeserver.data.payload.PayloadRepository;
import be.pbin.writeserver.data.payload.validation.InvalidPayloadException;
import be.pbin.writeserver.service.implementations.NoteServiceImpl;
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
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private MetadataRepository metadataRepository;
    @Mock
    private PayloadRepository payloadRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private NoteServiceImpl noteService;

    @Test
    void test_saveNote() throws InvalidPayloadException {
        String payload = RandomStringUtils.randomAlphabetic(10);
        String path_to_payload = RandomStringUtils.randomAlphabetic(10);
        int expiration = Integer.parseInt(RandomStringUtils.randomNumeric(3));
        NoteData note = new NoteData(expiration, payload);

        doReturn(path_to_payload).when(payloadRepository).savePayload(any(Payload.class));

        URI result = noteService.save(note);
        String uniqueNoteId = UriUtils.extractLastSegment(result);

        ArgumentCaptor<Payload> blobCaptor = ArgumentCaptor.forClass(Payload.class);
        verify(payloadRepository, times(1)).savePayload(blobCaptor.capture());

        Payload capturedPayload = blobCaptor.getValue();
        assertThat(capturedPayload.id()).isEqualTo(uniqueNoteId);
        assertThat(capturedPayload.payload()).isEqualTo(payload);

        verify(validationService, times(1)).validate(capturedPayload);

        verify(metadataRepository, times(1)).existsById(eq(uniqueNoteId));

        ArgumentCaptor<NoteMetaData> metaDataCaptor = ArgumentCaptor.forClass(NoteMetaData.class);
        verify(metadataRepository, times(1)).save(metaDataCaptor.capture());

        NoteMetaData capturedMetaData = metaDataCaptor.getValue();
        assertThat(capturedMetaData.getPath()).isEqualTo(path_to_payload);
        assertThat(capturedMetaData.getExpirationDate()).isCloseTo(LocalDateTime.now().plusMinutes(expiration), within(1, ChronoUnit.SECONDS));
        assertThat(capturedMetaData.getShortLink()).isEqualTo(uniqueNoteId);

        LocalDateTime justNow = LocalDateTime.now().minusSeconds(1);
        LocalDateTime now = LocalDateTime.now();
        assertThat(capturedMetaData.getCreationDate()).isBetween(justNow, now);
    }

    //todo: exception test
}