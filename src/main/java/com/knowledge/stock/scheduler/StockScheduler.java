package com.knowledge.stock.scheduler;

import com.knowledge.stock.service.StockProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.stock-schedule-enabled", havingValue = "true", matchIfMissing = true)
public class StockScheduler {

    private final StockProcessingService stockProcessingService;

    /**
     * Daily stock knowledge processing at 6:00 AM
     * Cron expression: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Ho_Chi_Minh")
    public void processStockKnowledge() {
        log.info("Stock knowledge processing scheduler triggered at 6:00 AM");

        try {
            stockProcessingService.processTodaysLearning();
            log.info("Stock knowledge processing completed successfully");

        } catch (Exception e) {
            log.error("Stock knowledge processing failed: {}", e.getMessage(), e);
            // Don't rethrow - we don't want to break the scheduler
        }
    }

//    /**
//     * Weekly comprehensive stock analysis - every Sunday at 7:00 AM
//     */
//    @Scheduled(cron = "0 0 7 * * SUN", zone = "Asia/Ho_Chi_Minh")
//    public void processWeeklyStockAnalysis() {
//        log.info("Weekly stock analysis scheduler triggered");
//
//        try {
//            stockProcessingService.processWeeklyStockAnalysis();
//            log.info("Weekly stock analysis completed successfully");
//
//        } catch (Exception e) {
//            log.error("Weekly stock analysis failed: {}", e.getMessage(), e);
//        }
//    }

    /**
     * Health check every 2 hours for stock service
     */
    @Scheduled(fixedRate = 7200000) // Every 2 hours
    public void stockHealthCheck() {
        log.debug("Stock Knowledge Service scheduler is running - Health check OK");
    }
}
