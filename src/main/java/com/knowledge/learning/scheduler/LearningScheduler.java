package com.knowledge.learning.scheduler;

import com.knowledge.learning.service.LearningProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.learning-schedule-enabled", havingValue = "true", matchIfMissing = true)
public class LearningScheduler {

    private final LearningProcessingService learningProcessingService;

    /**
     * Daily learning path processing at 7:00 AM
     * Cron expression: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Ho_Chi_Minh")
    public void processLearningPath() {
        log.info("6-Month Learning Path processing scheduler triggered at 7:00 AM");

        try {
            learningProcessingService.processTodaysLearning();
            log.info("6-Month Learning Path processing completed successfully");

        } catch (Exception e) {
            log.error("6-Month Learning Path processing failed: {}", e.getMessage(), e);
            // Don't rethrow - we don't want to break the scheduler
        }
    }

    /**
     * Health check every 3 hours for learning service
     */
    @Scheduled(fixedRate = 10800000) // Every 3 hours
    public void learningHealthCheck() {
        log.debug("6-Month Learning Path Service scheduler is running - Health check OK");
    }
}
