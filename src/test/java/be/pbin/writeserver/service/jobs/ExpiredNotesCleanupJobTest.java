package be.pbin.writeserver.service.jobs;

import be.pbin.writeserver.data.metadata.MetaData;
import be.pbin.writeserver.data.metadata.MetadataRepository;
import be.pbin.writeserver.data.payload.PayloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiredNotesCleanupJobTest {

    @Mock
    private MetadataRepository metadataRepository;
    @Mock
    private PayloadRepository payloadRepository;
    @InjectMocks
    private ExpiredNotesCleanupJob job;

    @Test
    void test_execute() {
        MetaData metaData1 = MetaData.builder().shortLink("shortlink1").build();
        MetaData metaData2 = MetaData.builder().shortLink("shortlink2").build();

        when(metadataRepository.findByExpirationDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(metaData1, metaData2));

        job.execute();

        verify(payloadRepository).deleteById("shortlink1");
        verify(payloadRepository).deleteById("shortlink2");
        verify(metadataRepository).deleteById("shortlink1");
        verify(metadataRepository).deleteById("shortlink2");
        verifyNoMoreInteractions(payloadRepository, metadataRepository);
    }
}
