package com.knowledge.topic.controller;

import com.knowledge.topic.dto.ProcessingStatsDto;
import com.knowledge.topic.model.Topic;
import com.knowledge.topic.service.TopicProcessingService;
import com.knowledge.topic.repository.TopicRepository;
import com.knowledge.topic.repository.KnowledgeContentRepository;
import com.knowledge.topic.model.TopicStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/topics")
@Slf4j
@RequiredArgsConstructor
public class TopicController {

    private final TopicProcessingService processingService;
    private final TopicRepository topicRepository;
    private final KnowledgeContentRepository contentRepository;

    /**
     * Manual trigger for immediate processing
     */
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerTopicProcessing() {
        log.info("Manual trigger received for topic processing");

        try {
            processingService.triggerImmediateProcessing();
            return ResponseEntity.ok("Topic processing triggered successfully. Check logs for details.");

        } catch (Exception e) {
            log.error("Manual trigger failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Topic processing failed: " + e.getMessage());
        }
    }

    /**
     * Process specific topics by name
     */
    @PostMapping("/process")
    public ResponseEntity<String> processSpecificTopics(@RequestBody List<String> topicNames) {
        log.info("Processing specific topics: {}", topicNames);

        if (topicNames == null || topicNames.isEmpty()) {
            return ResponseEntity.badRequest().body("Topic names list cannot be empty");
        }

        try {
            processingService.processSpecificTopics(topicNames);
            return ResponseEntity.ok(String.format("Successfully processed %d topics: %s",
                    topicNames.size(), String.join(", ", topicNames)));

        } catch (Exception e) {
            log.error("Specific topics processing failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Topic processing failed: " + e.getMessage());
        }
    }

    /**
     * Add new topics to Excel and database
     */
    @PostMapping("/add")
    public ResponseEntity<String> addNewTopics(@RequestBody List<Topic> topics) {
        log.info("Adding {} new topics", topics.size());

        if (topics == null || topics.isEmpty()) {
            return ResponseEntity.badRequest().body("Topics list cannot be empty");
        }

        try {
            processingService.addNewTopics(topics);
            return ResponseEntity.ok(String.format("Successfully added %d new topics", topics.size()));

        } catch (Exception e) {
            log.error("Adding topics failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Adding topics failed: " + e.getMessage());
        }
    }

    /**
     * Get processing statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ProcessingStatsDto> getProcessingStats() {
        log.debug("Retrieving processing statistics");

        try {
            ProcessingStatsDto stats = ProcessingStatsDto.builder()
                    .totalTopics(topicRepository.count())
                    .newTopics(topicRepository.countByStatus(TopicStatus.NEW))
                    .processedTopics(topicRepository.countByStatus(TopicStatus.DONE))
                    .errorTopics(topicRepository.countByStatus(TopicStatus.ERROR))
                    .archivedTopics(topicRepository.countByStatus(TopicStatus.ARCHIVED))
                    .processingTopics(topicRepository.countByStatus(TopicStatus.PROCESSING))
                    .totalContentGenerated(contentRepository.count())
                    .pendingEmailContent(contentRepository.findPendingEmailContent().size())
                    .averageOverviewWords(contentRepository.getAverageOverviewWordCount())
                    .averageDetailedWords(contentRepository.getAverageDetailedWordCount())
                    .contentGeneratedToday(contentRepository.countGeneratedContentSince(LocalDateTime.now().toLocalDate().atStartOfDay()))
                    .lastUpdated(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error retrieving statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Reset topic processing status
     */
    @PutMapping("/reset/{topicName}")
    public ResponseEntity<String> resetTopicStatus(@PathVariable String topicName) {
        log.info("Resetting status for topic: {}", topicName);

        if (topicName == null || topicName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Topic name cannot be empty");
        }

        try {
            processingService.resetTopicStatus(topicName.trim());
            return ResponseEntity.ok("Topic status reset successfully: " + topicName);

        } catch (Exception e) {
            log.error("Reset topic status failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Reset failed: " + e.getMessage());
        }
    }

    /**
     * Get all topics with their current status
     */
    @GetMapping("/list")
    public ResponseEntity<List<Topic>> getAllTopics() {
        log.debug("Retrieving all topics");

        try {
            List<Topic> topics = topicRepository.findAll();
            return ResponseEntity.ok(topics);

        } catch (Exception e) {
            log.error("Error retrieving topics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Get topics by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Topic>> getTopicsByStatus(@PathVariable String status) {
        log.debug("Retrieving topics with status: {}", status);

        try {
            TopicStatus topicStatus = TopicStatus.valueOf(status.toUpperCase());
            List<Topic> topics = topicRepository.findByStatus(topicStatus);
            return ResponseEntity.ok(topics);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);

        } catch (Exception e) {
            log.error("Error retrieving topics by status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Get topics processed in the last N days
     */
    @GetMapping("/recent/{days}")
    public ResponseEntity<List<Topic>> getRecentlyProcessedTopics(@PathVariable int days) {
        log.debug("Retrieving topics processed in the last {} days", days);

        if (days <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            List<Topic> topics = topicRepository.findProcessedSince(startDate);
            return ResponseEntity.ok(topics);

        } catch (Exception e) {
            log.error("Error retrieving recent topics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
