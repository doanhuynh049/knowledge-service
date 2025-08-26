package com.knowledge.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicOverview {

    private String topicName;
    private String category;
    private String introduction;
    private List<String> keyConcepts;
    private String currentRelevance;
    private List<String> mainTakeaways;
    private String whyItMatters;
    private int wordCount;

    public TopicOverview(String topicName, String category, String rawContent) {
        this.topicName = topicName;
        this.category = category;
        this.wordCount = rawContent != null ? rawContent.split("\\s+").length : 0;
        parseOverviewContent(rawContent);
    }

    private void parseOverviewContent(String content) {
        // Simple parsing logic - in a real implementation, you might use more sophisticated parsing
        if (content == null || content.trim().isEmpty()) {
            this.introduction = "Content not available";
            this.keyConcepts = List.of();
            this.currentRelevance = "Not specified";
            this.mainTakeaways = List.of();
            this.whyItMatters = "Not specified";
            return;
        }

        // For now, store the full content as introduction
        // In a production system, you'd implement proper content parsing
        this.introduction = content;
        this.keyConcepts = List.of("Key concepts extracted from content");
        this.currentRelevance = "Highly relevant in today's context";
        this.mainTakeaways = List.of("Important insights from the topic");
        this.whyItMatters = "Significant impact on understanding";
    }
}
