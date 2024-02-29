package be.pbin.writeserver.service.jobs;

import be.pbin.writeserver.data.metadata.MetaData;
import be.pbin.writeserver.data.MetadataRepository;
import be.pbin.writeserver.data.PayloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpiredNotesCleanupJob {

    Logger logger = LoggerFactory.getLogger(ExpiredNotesCleanupJob.class);

    private final MetadataRepository metadataRepository;
    private final PayloadRepository payloadRepository;

    public ExpiredNotesCleanupJob(MetadataRepository metadataRepository,
                                  PayloadRepository payloadRepository) {
        this.metadataRepository = metadataRepository;
        this.payloadRepository = payloadRepository;
    }

    @Scheduled(cron = "0 0 2 * * SUN") //every sunday at 2am
    public void execute() {
        List<MetaData> expiredNotes = metadataRepository.findByExpirationDateBefore(LocalDateTime.now());

        logger.info("Removing expired notes from repositories");

        expiredNotes.forEach(note -> {
                    payloadRepository.deleteById(note.getShortLink());
                    metadataRepository.deleteById(note.getShortLink());
                    });
    }
}
