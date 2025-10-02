package com.knowledge.learning.controller;

import com.knowledge.learning.model.LearningDay;
import com.knowledge.learning.service.LearningProcessingService;
import com.knowledge.learning.service.LearningExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-path")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LearningController {

    private final LearningProcessingService processingService;
    private final LearningExcelService excelService;

    /**
     * Process today's learning day
     */
    @PostMapping("/learn/today")
    public ResponseEntity<Map<String, Object>> processTodaysLearning() {
        log.info("🚀 Manual trigger for today's learning path");

        try {
            processingService.processTodaysLearning();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Today's learning processed successfully");
            response.put("timestamp", System.currentTimeMillis());

            log.info("✅ Today's learning completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Today's learning processing failed: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Today's learning failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Process specific day by number
     */
    @PostMapping("/learn/day/{dayNumber}")
    public ResponseEntity<Map<String, Object>> processSpecificDay(@PathVariable int dayNumber) {
        log.info("🎯 Manual processing requested for Day {}", dayNumber);

        try {
            processingService.processSpecificDay(dayNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Day " + dayNumber + " processed successfully");
            response.put("day", dayNumber);
            response.put("timestamp", System.currentTimeMillis());

            log.info("✅ Day {} processing completed successfully", dayNumber);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Day {} processing failed: {}", dayNumber, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Day " + dayNumber + " processing failed: " + e.getMessage());
            response.put("day", dayNumber);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Reset specific day for reprocessing
     */
    @PutMapping("/learn/day/{dayNumber}/reset")
    public ResponseEntity<Map<String, Object>> resetDay(@PathVariable int dayNumber) {
        log.info("🔄 Reset requested for Day {}", dayNumber);

        try {
            processingService.resetDay(dayNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Day " + dayNumber + " reset successfully");
            response.put("day", dayNumber);
            response.put("timestamp", System.currentTimeMillis());

            log.info("✅ Day {} reset completed successfully", dayNumber);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Day {} reset failed: {}", dayNumber, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Day " + dayNumber + " reset failed: " + e.getMessage());
            response.put("day", dayNumber);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get learning progress and statistics
     */
    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getLearningProgress() {
        log.debug("📊 Learning progress requested");

        try {
            LearningExcelService.LearningProgress progress = processingService.getLearningProgress();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("progress", progress);
            response.put("timestamp", System.currentTimeMillis());

            log.info("📈 Progress retrieved: {}% complete ({}/{} days)",
                String.format("%.1f", progress.getCompletionRate()),
                progress.getCompletedDays(),
                progress.getTotalDays());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error retrieving learning progress: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Progress retrieval failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get all learning days with their status
     */
    @GetMapping("/curriculum")
    public ResponseEntity<Map<String, Object>> getCurriculum() {
        log.debug("📚 Curriculum overview requested");

        try {
            List<LearningDay> allDays = excelService.getAllLearningDays();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("curriculum", allDays);
            response.put("totalDays", allDays.size());
            response.put("timestamp", System.currentTimeMillis());

            log.info("📋 Curriculum retrieved with {} learning days", allDays.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error retrieving curriculum: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Curriculum retrieval failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get upcoming days preview
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingDays(@RequestParam(defaultValue = "5") int count) {
        log.debug("🔍 Upcoming days requested (count: {})", count);

        try {
            List<LearningDay> upcomingDays = processingService.getUpcomingDays(count);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("upcomingDays", upcomingDays);
            response.put("count", upcomingDays.size());
            response.put("requestedCount", count);
            response.put("timestamp", System.currentTimeMillis());

            log.info("📅 Found {} upcoming days", upcomingDays.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error retrieving upcoming days: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Upcoming days retrieval failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get next learning day details
     */
    @GetMapping("/next")
    public ResponseEntity<Map<String, Object>> getNextLearningDay() {
        log.debug("🔍 Next learning day requested");

        try {
            LearningDay nextDay = excelService.getNextLearningDay();

            Map<String, Object> response = new HashMap<>();

            if (nextDay != null) {
                response.put("success", true);
                response.put("nextDay", nextDay);
                response.put("hasNext", true);
                log.info("📅 Next day: Day {} - {}", nextDay.getDay(), nextDay.getPhase());
            } else {
                response.put("success", true);
                response.put("nextDay", null);
                response.put("hasNext", false);
                response.put("message", "All learning days completed!");
                log.info("🎉 All learning days completed");
            }

            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error retrieving next learning day: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Next day retrieval failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get learning days by phase
     */
    @GetMapping("/phase/{phase}")
    public ResponseEntity<Map<String, Object>> getLearningDaysByPhase(@PathVariable String phase) {
        log.debug("📖 Learning days requested for phase: {}", phase);

        try {
            List<LearningDay> allDays = excelService.getAllLearningDays();
            List<LearningDay> phaseDays = allDays.stream()
                .filter(day -> phase.equalsIgnoreCase(day.getPhase()))
                .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("phase", phase);
            response.put("days", phaseDays);
            response.put("count", phaseDays.size());
            response.put("timestamp", System.currentTimeMillis());

            log.info("📚 Found {} days for phase: {}", phaseDays.size(), phase);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error retrieving days for phase {}: {}", phase, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Phase retrieval failed: " + e.getMessage());
            response.put("phase", phase);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Health check for learning path service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("🏥 Health check requested for learning path service");

        try {
            Map<String, Object> health = new HashMap<>();
            health.put("service", "6-Month Learning Path Service");
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());

            // Get basic stats
            LearningExcelService.LearningProgress progress = processingService.getLearningProgress();
            health.put("totalDays", progress.getTotalDays());
            health.put("completedDays", progress.getCompletedDays());
            health.put("completionRate", String.format("%.1f%%", progress.getCompletionRate()));

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("❌ Health check failed: {}", e.getMessage(), e);

            Map<String, Object> health = new HashMap<>();
            health.put("service", "6-Month Learning Path Service");
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(health);
        }
    }
}
