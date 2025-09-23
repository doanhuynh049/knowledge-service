package com.knowledge.stock.controller;

import com.knowledge.stock.model.StockLearningDay;
import com.knowledge.stock.service.StockExcelService;
import com.knowledge.stock.service.StockProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enhanced-stock")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockController {

    private final StockProcessingService processingService;
    private final StockExcelService excelService;

    /**
     * Process today's learning day
     */
    @PostMapping("/learn/today")
    public ResponseEntity<Map<String, Object>> processTodaysLearning() {
        log.info("🚀 Manual trigger for today's enhanced stock learning");

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
            response.put("dayNumber", dayNumber);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Day {} processing failed: {}", dayNumber, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Day " + dayNumber + " processing failed: " + e.getMessage());
            response.put("dayNumber", dayNumber);
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
            response.put("dayNumber", dayNumber);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Day {} reset failed: {}", dayNumber, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Day " + dayNumber + " reset failed: " + e.getMessage());
            response.put("dayNumber", dayNumber);
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
            StockExcelService.LearningProgress progress = processingService.getLearningProgress();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalDays", progress.getTotalDays());
            response.put("completedDays", progress.getCompletedDays());
            response.put("errorDays", progress.getErrorDays());
            response.put("openDays", progress.getOpenDays());
            response.put("completionRate", progress.getCompletionRate());
            response.put("timestamp", System.currentTimeMillis());

            log.info("📈 Progress retrieved: {}% complete ({}/{})",
                String.format("%.1f", progress.getCompletionRate()),
                progress.getCompletedDays(),
                progress.getTotalDays());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error retrieving progress: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving progress: " + e.getMessage());
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
            List<StockLearningDay> allDays = excelService.getAllLearningDays();

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
            response.put("message", "Error retrieving curriculum: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get upcoming days preview
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingDays(@RequestParam(defaultValue = "5") int count) {
        log.debug("👀 Upcoming days preview requested (limit: {})", count);

        try {
            List<StockLearningDay> upcomingDays = processingService.getUpcomingDays(count);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("upcomingDays", upcomingDays);
            response.put("count", upcomingDays.size());
            response.put("requestedCount", count);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error retrieving upcoming days: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving upcoming days: " + e.getMessage());
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
            StockLearningDay nextDay = excelService.getNextLearningDay();

            Map<String, Object> response = new HashMap<>();

            if (nextDay != null) {
                response.put("success", true);
                response.put("nextDay", nextDay);
                response.put("hasNext", true);
                log.info("📅 Next day: Day {} - {}", nextDay.getDay(), nextDay.getTopic());
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
            response.put("message", "Error retrieving next learning day: " + e.getMessage());
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
            List<StockLearningDay> allDays = excelService.getAllLearningDays();
            List<StockLearningDay> phaseDays = allDays.stream()
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
            response.put("message", "Error retrieving days for phase: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Health check for enhanced stock service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("🏥 Enhanced stock service health check");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Enhanced Stock Learning Service");
        response.put("features", List.of(
            "Structured 20-day curriculum",
            "Phase-based learning progression",
            "Comprehensive logging",
            "Practice task integration",
            "Progress tracking"
        ));
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
