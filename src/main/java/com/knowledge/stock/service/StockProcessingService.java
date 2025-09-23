package com.knowledge.stock.service;

import com.knowledge.stock.model.StockLearningDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockProcessingService {

    private final StockExcelService excelService;
    private final StockContentGenerationService contentGenerationService;
    private final StockEmailService emailService;

    /**
     * Process today's learning day with comprehensive logging
     */
    public void processTodaysLearning() {
        log.info("🚀 Starting enhanced stock learning processing...");

        try {
            // Step 1: Get next learning day
            StockLearningDay todaysLesson = excelService.getNextLearningDay();

            if (todaysLesson == null) {
                log.warn("🎓 All learning days completed! No more lessons to process.");
                return;
            }

            log.info("📅 Processing Day {}: {} (Phase: {})",
                todaysLesson.getDay(),
                todaysLesson.getTopic(),
                todaysLesson.getPhase());

            // Step 2: Process the learning day
            processLearningDay(todaysLesson);

            log.info("✅ Successfully completed processing for Day {}", todaysLesson.getDay());

        } catch (Exception e) {
            log.error("❌ Enhanced stock learning processing failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Process a specific learning day
     */
    private void processLearningDay(StockLearningDay learningDay) {
        int day = learningDay.getDay();
        String topic = learningDay.getTopic();

        log.info("🔄 Starting processing for Day {}: {}", day, topic);

        try {
            // Step 1: Generate AI prompt specific to the learning goal
            log.debug("🧠 Generating AI prompt for: {}", learningDay.getLearningGoal());
            String prompt = contentGenerationService.generateStructuredStockPrompt(learningDay);

            // Step 2: Get AI response
            log.debug("🤖 Requesting AI response for Day {} content...", day);
            String aiResponse = contentGenerationService.getAIResponse(prompt);
            log.info("✅ AI response generated for Day {} ({} characters)", day, aiResponse.length());

            // Step 3: Send structured email
            log.debug("📧 Preparing structured email for Day {}...", day);
            emailService.sendStructuredLearningEmail(learningDay, aiResponse);
            log.info("📨 Learning email sent successfully for Day {}", day);

            // Step 4: Mark as completed
            String completionNotes = String.format("Completed on %s. AI content: %d chars. Email sent successfully.",
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                aiResponse.length());

            excelService.markLearningDayCompleted(day, completionNotes);

            // Step 5: Log progress
            logProgress();

        } catch (Exception e) {
            log.error("❌ Error processing Day {}: {}", day, e.getMessage(), e);
            excelService.markLearningDayError(day, e.getMessage());
            throw e;
        }
    }

    /**
     * Get current learning progress with detailed logging
     */
    public StockExcelService.LearningProgress getLearningProgress() {
        log.debug("📊 Retrieving learning progress...");

        StockExcelService.LearningProgress progress = excelService.getLearningProgress();

        log.info("📈 Current Progress Summary:");
        log.info("   Total Days: {}", progress.getTotalDays());
        log.info("   Completed: {} ({}%)", progress.getCompletedDays(),
            String.format("%.1f", progress.getCompletionRate()));
        log.info("   Remaining: {}", progress.getOpenDays());
        log.info("   Errors: {}", progress.getErrorDays());

        return progress;
    }

    /**
     * Log current progress after each completion
     */
    private void logProgress() {
        StockExcelService.LearningProgress progress = excelService.getLearningProgress();

        log.info("🎯 Learning Progress Update: {}/{} days completed ({}%)",
            progress.getCompletedDays(),
            progress.getTotalDays(),
            String.format("%.1f", progress.getCompletionRate()));

        if (progress.getOpenDays() > 0) {
            log.info("📚 Next: {} more days remaining", progress.getOpenDays());
        } else {
            log.info("🎉 Congratulations! All learning days completed!");
        }
    }

    /**
     * Process specific day by number (for manual triggers)
     */
    public void processSpecificDay(int dayNumber) {
        log.info("🎯 Manual processing requested for Day {}", dayNumber);

        try {
            java.util.List<StockLearningDay> allDays = excelService.getAllLearningDays();
            StockLearningDay targetDay = allDays.stream()
                .filter(day -> day.getDay() == dayNumber)
                .findFirst()
                .orElse(null);

            if (targetDay == null) {
                log.error("❌ Day {} not found in curriculum", dayNumber);
                throw new IllegalArgumentException("Day " + dayNumber + " not found");
            }

            log.info("📋 Found Day {}: {} (Current status: {})",
                dayNumber, targetDay.getTopic(), targetDay.getStatus());

            processLearningDay(targetDay);

        } catch (Exception e) {
            log.error("❌ Manual processing failed for Day {}: {}", dayNumber, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Reset a specific day for reprocessing
     */
    public void resetDay(int dayNumber) {
        log.info("🔄 Resetting Day {} for reprocessing...", dayNumber);

        try {
            excelService.updateLearningDayStatus(dayNumber, "OPEN", "Reset for reprocessing", "");
            log.info("✅ Day {} reset successfully - ready for reprocessing", dayNumber);

        } catch (Exception e) {
            log.error("❌ Failed to reset Day {}: {}", dayNumber, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get next few days preview
     */
    public java.util.List<StockLearningDay> getUpcomingDays(int count) {
        log.debug("👀 Getting preview of next {} upcoming days...", count);

        java.util.List<StockLearningDay> allDays = excelService.getAllLearningDays();
        java.util.List<StockLearningDay> upcomingDays = allDays.stream()
            .filter(day -> "OPEN".equals(day.getStatus()))
            .limit(count)
            .collect(java.util.stream.Collectors.toList());

        log.info("📅 Found {} upcoming days to preview", upcomingDays.size());
        upcomingDays.forEach(day ->
            log.info("   Day {}: {} ({})", day.getDay(), day.getTopic(), day.getPhase()));

        return upcomingDays;
    }
}
