package com.knowledge.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicDetail {

    private String topicName;
    private String category;
    private String comprehensiveIntroduction;
    private String historicalContext;
    private String coreMechanisms;
    private String technicalDetails;
    private List<String> realWorldApplications;
    private List<String> caseStudies;
    private String relatedConcepts;
    private String currentTrends;
    private String futureOutlook;
    private String expertInsights;
    private List<String> furtherLearningResources;
    private int wordCount;

    public TopicDetail(String topicName, String category, String rawContent) {
        this.topicName = topicName;
        this.category = category;
        this.wordCount = rawContent != null ? rawContent.split("\\s+").length : 0;
        parseDetailedContent(rawContent);
    }

    private void parseDetailedContent(String content) {
        // Simple parsing logic - in a real implementation, you might use more sophisticated parsing
        if (content == null || content.trim().isEmpty()) {
            setDefaultValues();
            return;
        }

        // For now, distribute content across sections
        // In a production system, you'd implement proper content parsing with AI or regex
        String[] sections = content.split("\n\n");

        this.comprehensiveIntroduction = sections.length > 0 ? sections[0] : "Detailed introduction not available";
        this.historicalContext = sections.length > 1 ? sections[1] : "Historical context will be provided";
        this.coreMechanisms = sections.length > 2 ? sections[2] : "Core mechanisms explained";
        this.technicalDetails = sections.length > 3 ? sections[3] : "Technical details provided";
        this.realWorldApplications = List.of("Application 1", "Application 2", "Application 3");
        this.caseStudies = List.of("Case Study 1", "Case Study 2");
        this.relatedConcepts = sections.length > 4 ? sections[4] : "Related concepts explored";
        this.currentTrends = "Current trends and developments";
        this.futureOutlook = "Future predictions and outlook";
        this.expertInsights = "Expert perspectives and insights";
        this.furtherLearningResources = List.of("Resource 1", "Resource 2", "Resource 3");
    }

    private void setDefaultValues() {
        this.comprehensiveIntroduction = "Content not available";
        this.historicalContext = "Historical context not available";
        this.coreMechanisms = "Core mechanisms not available";
        this.technicalDetails = "Technical details not available";
        this.realWorldApplications = List.of();
        this.caseStudies = List.of();
        this.relatedConcepts = "Related concepts not available";
        this.currentTrends = "Current trends not available";
        this.futureOutlook = "Future outlook not available";
        this.expertInsights = "Expert insights not available";
        this.furtherLearningResources = List.of();
    }
}
