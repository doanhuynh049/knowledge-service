package com.knowledge.topic.service;

import com.knowledge.topic.dto.TopicDetail;
import com.knowledge.topic.dto.TopicOverview;
import com.knowledge.topic.model.KnowledgeContent;
import com.knowledge.topic.model.Topic;
import com.knowledge.topic.model.TopicStatus;
import com.knowledge.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicProcessingService {

    private final TopicRepository topicRepository;
    private final TopicExcelService excelService;
    private final ContentGenerationService contentService;
    private final TopicEmailService emailService;

    @Value("${app.daily-topic-limit}")
    private int dailyTopicLimit;

    @Value("${app.reprocess-after-days}")
    private int reprocessAfterDays;

    @Transactional
    public void processDailyTopics() {
        log.info("Starting daily topic processing with limit: {}", dailyTopicLimit);

        try {
            // Get next unprocessed topic from Excel
            Topic topicToProcess = getNextTopicForProcessing();

            if (topicToProcess == null) {
                log.info("No topics available for processing today");
                return;
            }

            // Process the single topic
            processTopics(List.of(topicToProcess));

            log.info("Daily topic processing completed successfully");

        } catch (Exception e) {
            log.error("Error during daily topic processing: {}", e.getMessage(), e);
            throw new RuntimeException("Daily topic processing failed", e);
        }
    }

    @Transactional
    public void processSpecificTopics(List<String> topicNames) {
        log.info("Processing specific topics: {}", topicNames);

        List<Topic> topicsToProcess = topicNames.stream()
                .map(this::findOrCreateTopic)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (topicsToProcess.isEmpty()) {
            log.warn("No valid topics found for processing");
            return;
        }

        processTopics(topicsToProcess);
    }

    @Transactional
    public void triggerImmediateProcessing() {
        log.info("Triggering immediate topic processing");

        // Get next topic for processing
        Topic topicToProcess = getNextTopicForProcessing();

        if (topicToProcess == null) {
            log.info("No topics available for immediate processing");
            return;
        }

        processTopics(List.of(topicToProcess));
        log.info("Immediate topic processing completed");
    }

    private void processTopics(List<Topic> topics) {
        log.info("Processing topics: {}", topics.stream().map(Topic::getName).toList());

        try {
            // Mark topics as processing
            markTopicsAsProcessing(topics);

            // Generate content for all topics
            List<KnowledgeContent> generatedContents = contentService.generateContentForTopics(topics);

            if (generatedContents.isEmpty()) {
                log.error("No content was generated for any topics");
                markTopicsAsError(topics);
                return;
            }

            // Create structured content for emails
            List<TopicOverview> overviews = contentService.createTopicOverviews(topics, generatedContents);
            List<TopicDetail> details = contentService.createTopicDetails(topics, generatedContents);

            // Send dual emails
            emailService.sendDualTopicEmails(topics, overviews, details);

            // Mark content as email sent
            contentService.markContentAsEmailSent(generatedContents);

            // Update topics as processed
            markTopicsAsCompleted(topics);

            // Log to Excel
            logProcessedTopicsToExcel(topics, generatedContents);

            log.info("Successfully processed {} topics and sent emails", topics.size());

        } catch (Exception e) {
            log.error("Error processing topics: {}", e.getMessage(), e);
            markTopicsAsError(topics);
            throw new RuntimeException("Topic processing failed", e);
        }
    }

    private Topic getNextTopicForProcessing() {
        // First try to get from Excel file
        Topic excelTopic = excelService.getNextUnprocessedTopic();
        if (excelTopic != null) {
            // Save to database if not exists
            Optional<Topic> existingTopic = topicRepository.findByName(excelTopic.getName());
            if (existingTopic.isEmpty()) {
                Topic savedTopic = topicRepository.save(excelTopic);
                log.info("Saved new topic from Excel: {}", savedTopic.getName());
                return savedTopic;
            } else {
                Topic existing = existingTopic.get();
                if (existing.getStatus() == TopicStatus.NEW ||
                    (existing.getLastProcessed() != null &&
                     existing.getLastProcessed().isBefore(LocalDateTime.now().minusDays(reprocessAfterDays)))) {
                    return existing;
                }
            }
        }

        // If no Excel topic, get from database
        List<Topic> unprocessedTopics = topicRepository.findNextTopicsToProcess(TopicStatus.NEW);
        if (!unprocessedTopics.isEmpty()) {
            return unprocessedTopics.get(0);
        }

        // Check for topics that can be reprocessed
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(reprocessAfterDays);
        List<Topic> reprocessableTopics = topicRepository.findUnprocessedTopicsSince(TopicStatus.DONE, cutoffDate);
        if (!reprocessableTopics.isEmpty()) {
            Topic topic = reprocessableTopics.get(0);
            topic.setStatus(TopicStatus.NEW);
            return topicRepository.save(topic);
        }

        return null;
    }

    private Optional<Topic> findOrCreateTopic(String topicName) {
        Optional<Topic> existing = topicRepository.findByName(topicName);
        if (existing.isPresent()) {
            return existing;
        }

        // Create new topic with default values
        Topic newTopic = new Topic(topicName, "General", 3, "Manually requested topic");
        Topic savedTopic = topicRepository.save(newTopic);
        log.info("Created new topic: {}", savedTopic.getName());
        return Optional.of(savedTopic);
    }

    private void markTopicsAsProcessing(List<Topic> topics) {
        for (Topic topic : topics) {
            topic.setStatus(TopicStatus.PROCESSING);
            topicRepository.save(topic);
        }
        log.info("Marked topics as processing: {}", topics.stream().map(Topic::getName).toList());
    }

    private void markTopicsAsCompleted(List<Topic> topics) {
        LocalDateTime now = LocalDateTime.now();
        for (Topic topic : topics) {
            topic.setStatus(TopicStatus.DONE);
            topic.setLastProcessed(now);
            topicRepository.save(topic);
        }

        // Also update in Excel
        excelService.markTopicsAsProcessed(topics);

        log.info("Marked {} topics as completed", topics.size());
    }

    private void markTopicsAsError(List<Topic> topics) {
        for (Topic topic : topics) {
            topic.setStatus(TopicStatus.ERROR);
            topicRepository.save(topic);
        }
        log.info("Marked {} topics as error", topics.size());
    }

    private void logProcessedTopicsToExcel(List<Topic> topics, List<KnowledgeContent> contents) {
        for (int i = 0; i < topics.size() && i < contents.size(); i++) {
            Topic topic = topics.get(i);
            KnowledgeContent content = contents.get(i);

            excelService.logProcessedContent(
                    topic,
                    content.getOverviewContent(),
                    content.getDetailedContent()
            );
        }
    }

    public void addNewTopics(List<Topic> topics) {
        log.info("Adding {} new topics", topics.size());

        // Save to database
        List<Topic> savedTopics = topicRepository.saveAll(topics);

        // Add to Excel
        excelService.addTopics(savedTopics);

        log.info("Successfully added {} topics to database and Excel", savedTopics.size());
    }

    public void resetTopicStatus(String topicName) {
        Optional<Topic> topicOpt = topicRepository.findByName(topicName);
        if (topicOpt.isPresent()) {
            Topic topic = topicOpt.get();
            topic.setStatus(TopicStatus.NEW);
            topic.setLastProcessed(null);
            topicRepository.save(topic);
            log.info("Reset status for topic: {}", topicName);
        } else {
            log.warn("Topic not found for reset: {}", topicName);
        }
    }
}
