package com.knowledge.topic.scheduler;

import com.knowledge.topic.service.TopicProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.schedule-enabled", havingValue = "true", matchIfMissing = true)
public class TopicScheduler {

    private final TopicProcessingService processingService;

    /**
     * Daily processing at 5:00 AM
     * Cron expression: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 5 * * *", zone = "America/New_York")
    public void processDailyTopics() {
        log.info("Daily topic processing scheduler triggered at 5:00 AM");

        try {
            processingService.processDailyTopics();
            log.info("Daily topic processing completed successfully");

        } catch (Exception e) {
            log.error("Daily topic processing failed: {}", e.getMessage(), e);
            // Don't rethrow - we don't want to break the scheduler
        }
    }

    /**
     * Health check every hour to ensure system is running
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void healthCheck() {
        log.debug("Topic Knowledge Service scheduler is running - Health check OK");
    }
}
