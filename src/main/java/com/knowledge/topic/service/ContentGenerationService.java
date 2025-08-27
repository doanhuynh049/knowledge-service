package com.knowledge.topic.service;

import com.knowledge.topic.dto.TopicDetail;
import com.knowledge.topic.dto.TopicOverview;
import com.knowledge.topic.model.KnowledgeContent;
import com.knowledge.topic.model.Topic;
import com.knowledge.topic.repository.KnowledgeContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentGenerationService {

    private final GeminiTopicClient geminiClient;
    private final KnowledgeContentRepository contentRepository;

    @Transactional
    public KnowledgeContent generateContent(Topic topic) {
        log.info("Starting content generation for topic: {}", topic.getName());
        long startTime = System.currentTimeMillis();

        try {
            // Generate overview content
            log.info("Generating overview content for: {}", topic.getName());
            // String overviewContent = geminiClient.generateOverviewContent(topic);
            String overviewContent = "";
            // Generate detailed content
            log.info("Generating detailed content for: {}", topic.getName());
            String detailedContent = geminiClient.generateDetailedContent(topic);
            log.info("Detailed content generated: {}", detailedContent);
            // Create knowledge content entity
            KnowledgeContent content = new KnowledgeContent(topic, overviewContent, detailedContent);

            // Calculate generation time
            long endTime = System.currentTimeMillis();
            content.setGenerationTimeSeconds((int) ((endTime - startTime) / 1000));

            // Save to database
            KnowledgeContent savedContent = contentRepository.save(content);

            log.info("Successfully generated content for topic: {} (Overview: {} words, Detailed: {} words, Time: {}s)",
                    topic.getName(),
                    savedContent.getOverviewWordCount(),
                    savedContent.getDetailedWordCount(),
                    savedContent.getGenerationTimeSeconds());

            return savedContent;

        } catch (Exception e) {
            log.error("Error generating content for topic {}: {}", topic.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate content for topic: " + topic.getName(), e);
        }
    }

    @Transactional
    public List<KnowledgeContent> generateContentForTopics(List<Topic> topics) {
        List<KnowledgeContent> generatedContent = new ArrayList<>();

        for (Topic topic : topics) {
            try {
                KnowledgeContent content = generateContent(topic);
                generatedContent.add(content);

                // Add small delay between generations to be respectful to API
                Thread.sleep(2000);

            } catch (Exception e) {
                log.error("Failed to generate content for topic: {}", topic.getName(), e);
                // Continue with other topics even if one fails
            }
        }

        log.info("Generated content for {}/{} topics", generatedContent.size(), topics.size());
        return generatedContent;
    }

    public TopicOverview createTopicOverview(Topic topic, KnowledgeContent content) {
        return new TopicOverview(
                topic.getName(),
                topic.getCategory(),
                content.getOverviewContent()
        );
    }

    public TopicDetail createTopicDetail(Topic topic, KnowledgeContent content) {
        return new TopicDetail(
                topic.getName(),
                topic.getCategory(),
                content.getDetailedContent()
        );
    }

    public List<TopicOverview> createTopicOverviews(List<Topic> topics, List<KnowledgeContent> contents) {
        List<TopicOverview> overviews = new ArrayList<>();

        for (int i = 0; i < topics.size() && i < contents.size(); i++) {
            TopicOverview overview = createTopicOverview(topics.get(i), contents.get(i));
            overviews.add(overview);
        }

        return overviews;
    }

    public List<TopicDetail> createTopicDetails(List<Topic> topics, List<KnowledgeContent> contents) {
        List<TopicDetail> details = new ArrayList<>();

        for (int i = 0; i < topics.size() && i < contents.size(); i++) {
            TopicDetail detail = createTopicDetail(topics.get(i), contents.get(i));
            details.add(detail);
        }

        return details;
    }

    @Transactional
    public void markContentAsEmailSent(List<KnowledgeContent> contents) {
        for (KnowledgeContent content : contents) {
            content.setEmailSent(true);
            content.setEmailSentAt(LocalDateTime.now());
            contentRepository.save(content);
        }

        log.info("Marked {} content items as email sent", contents.size());
    }

    public List<KnowledgeContent> getPendingEmailContent() {
        return contentRepository.findPendingEmailContent();
    }
}
