package com.knowledge.learning.service;

import com.knowledge.learning.model.LearningDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningProcessingService {

    private final LearningExcelService excelService;
    private final LearningContentGenerationService contentGenerationService;
    private final LearningEmailService emailService;

    /**
     * Process today's learning day with comprehensive logging
     */
    public void processTodaysLearning() {
        log.info("🚀 Starting 6-month learning path processing...");

        try {
            // Step 1: Get next learning day
            LearningDay todaysLesson = excelService.getNextLearningDay();

            if (todaysLesson == null) {
                log.warn("🎓 All learning days completed! No more lessons to process.");
                return;
            }

            log.info("📅 Processing Day {}: {} (Phase: {})",
                todaysLesson.getDay(),
                todaysLesson.getPhase(),
                todaysLesson.getWeek());

            // Step 2: Process the learning day
            processLearningDay(todaysLesson);

            log.info("✅ Successfully completed processing for Day {}", todaysLesson.getDay());

        } catch (Exception e) {
            log.error("❌ 6-month learning path processing failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Process a specific learning day
     */
    private void processLearningDay(LearningDay learningDay) {
        int day = learningDay.getDay();
        String phase = learningDay.getPhase();

        log.info("🔄 Starting processing for Day {}: {}", day, phase);

        try {
            // Step 1: Generate AI prompt specific to the learning goals
            log.info("🧠 Generating AI prompt for: {}", learningDay.getLearningGoal());
            String prompt = contentGenerationService.generateStructuredLearningPrompt(learningDay);
            log.info("✅ AI prompt generated for Day {} prompt: {} ", day, prompt);
            // Step 2: Get AI response
            log.info("🤖 Requesting AI response for Day {} content...", day);
            String aiResponse = contentGenerationService.getAIResponse(prompt);
            log.info("✅ AI response generated for Day {} response: {} ", day, aiResponse);

            // Step 3: Send structured email
            log.info("📧 Preparing structured email for Day {}...", day);
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
            // Mark as error in Excel
            excelService.markLearningDayError(day, e.getMessage());
            throw e;
        }
    }

    /**
     * Get current learning progress with detailed logging
     */
    public LearningExcelService.LearningProgress getLearningProgress() {
        log.info("📊 Retrieving learning progress...");

        LearningExcelService.LearningProgress progress = excelService.getLearningProgress();

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
        LearningExcelService.LearningProgress progress = excelService.getLearningProgress();

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
            java.util.List<LearningDay> allDays = excelService.getAllLearningDays();
            LearningDay targetDay = allDays.stream()
                .filter(day -> day.getDay() == dayNumber)
                .findFirst()
                .orElse(null);

            if (targetDay == null) {
                log.error("❌ Day {} not found in curriculum", dayNumber);
                throw new IllegalArgumentException("Day " + dayNumber + " not found");
            }

            log.info("📋 Found Day {}: {} (Current status: {})",
                dayNumber, targetDay.getPhase(), targetDay.getStatus());

            processLearningDay(targetDay);

        } catch (Exception e) {
            log.error("❌ Failed to process specific Day {}: {}", dayNumber, e.getMessage(), e);
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
    public java.util.List<LearningDay> getUpcomingDays(int count) {
        log.info("👀 Getting preview of next {} upcoming days...", count);

        java.util.List<LearningDay> allDays = excelService.getAllLearningDays();
        java.util.List<LearningDay> upcomingDays = allDays.stream()
            .filter(day -> "OPEN".equals(day.getStatus()))
            .limit(count)
            .collect(java.util.stream.Collectors.toList());

        log.info("📅 Found {} upcoming days to preview", upcomingDays.size());
        upcomingDays.forEach(day ->
            log.info("   Day {}: {} ({})", day.getDay(), day.getPhase(), day.getWeek()));

        return upcomingDays;
    }
}
