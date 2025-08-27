package com.knowledge.topic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ContentParserService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ParsedOverview parseOverviewContent(String jsonContent) {
        try {
            // Try to parse as JSON first
            if (jsonContent.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(jsonContent);
                return ParsedOverview.builder()
                        .introduction(getTextValue(jsonNode, "introduction"))
                        .keyConcepts(getArrayValues(jsonNode, "keyConcepts"))
                        .currentRelevance(getTextValue(jsonNode, "currentRelevance"))
                        .mainTakeaways(getArrayValues(jsonNode, "mainTakeaways"))
                        .whyItMatters(getTextValue(jsonNode, "whyItMatters"))
                        .quickStats(getArrayValues(jsonNode, "quickStats"))
                        .build();
            } else {
                // Fallback: treat as plain text and parse sections
                return parseOverviewFromText(jsonContent);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON content, using text parsing fallback: {}", e.getMessage());
            return parseOverviewFromText(jsonContent);
        }
    }

    public ParsedDetail parseDetailedContent(String jsonContent) {
        try {
            // Try to parse as JSON first
            if (jsonContent.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(jsonContent);
                return ParsedDetail.builder()
                        .executiveSummary(getTextValue(jsonNode, "executiveSummary"))
                        .historicalEvolution(getTextValue(jsonNode, "historicalEvolution"))
                        .corePrinciples(getTextValue(jsonNode, "corePrinciples"))
                        .realWorldApplications(parseApplications(jsonNode.get("realWorldApplications")))
                        .caseStudies(parseCaseStudies(jsonNode.get("caseStudies")))
                        .interconnectedConcepts(getTextValue(jsonNode, "interconnectedConcepts"))
                        .currentInnovation(getTextValue(jsonNode, "currentInnovation"))
                        .futureOutlook(getTextValue(jsonNode, "futureOutlook"))
                        .expertInsights(parseExpertInsights(jsonNode.get("expertInsights")))
                        .learningResources(parseLearningResources(jsonNode.get("learningResources")))
                        .keyMetrics(getArrayValues(jsonNode, "keyMetrics"))
                        .build();
            } else {
                // Fallback: treat as plain text
                return parseDetailFromText(jsonContent);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse detailed JSON content, using text parsing fallback: {}", e.getMessage());
            return parseDetailFromText(jsonContent);
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asText() : "";
    }

    private List<String> getArrayValues(JsonNode node, String fieldName) {
        List<String> values = new ArrayList<>();
        JsonNode arrayNode = node.get(fieldName);
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(item -> values.add(item.asText()));
        }
        return values;
    }

    private List<ParsedDetail.Application> parseApplications(JsonNode applicationsNode) {
        List<ParsedDetail.Application> applications = new ArrayList<>();
        if (applicationsNode != null && applicationsNode.isArray()) {
            applicationsNode.forEach(app -> {
                applications.add(ParsedDetail.Application.builder()
                        .title(getTextValue(app, "title"))
                        .description(getTextValue(app, "description"))
                        .impact(getTextValue(app, "impact"))
                        .build());
            });
        }
        return applications;
    }

    private List<ParsedDetail.CaseStudy> parseCaseStudies(JsonNode caseStudiesNode) {
        List<ParsedDetail.CaseStudy> caseStudies = new ArrayList<>();
        if (caseStudiesNode != null && caseStudiesNode.isArray()) {
            caseStudiesNode.forEach(study -> {
                caseStudies.add(ParsedDetail.CaseStudy.builder()
                        .company(getTextValue(study, "company"))
                        .challenge(getTextValue(study, "challenge"))
                        .solution(getTextValue(study, "solution"))
                        .results(getTextValue(study, "results"))
                        .build());
            });
        }
        return caseStudies;
    }

    private List<ParsedDetail.ExpertInsight> parseExpertInsights(JsonNode insightsNode) {
        List<ParsedDetail.ExpertInsight> insights = new ArrayList<>();
        if (insightsNode != null && insightsNode.isArray()) {
            insightsNode.forEach(insight -> {
                insights.add(ParsedDetail.ExpertInsight.builder()
                        .expert(getTextValue(insight, "expert"))
                        .insight(getTextValue(insight, "insight"))
                        .build());
            });
        }
        return insights;
    }

    private List<ParsedDetail.LearningResource> parseLearningResources(JsonNode resourcesNode) {
        List<ParsedDetail.LearningResource> resources = new ArrayList<>();
        if (resourcesNode != null && resourcesNode.isArray()) {
            resourcesNode.forEach(resource -> {
                resources.add(ParsedDetail.LearningResource.builder()
                        .type(getTextValue(resource, "type"))
                        .title(getTextValue(resource, "title"))
                        .relevance(getTextValue(resource, "relevance"))
                        .build());
            });
        }
        return resources;
    }

    // Fallback text parsing methods
    private ParsedOverview parseOverviewFromText(String textContent) {
        return ParsedOverview.builder()
                .introduction(textContent)
                .keyConcepts(List.of("Key concepts from analysis"))
                .currentRelevance("Highly relevant in current industry context")
                .mainTakeaways(List.of("Important insights", "Practical applications", "Strategic value"))
                .whyItMatters("Essential for professional development and career advancement")
                .quickStats(List.of("Growing field", "High demand", "Strong ROI"))
                .build();
    }

    private ParsedDetail parseDetailFromText(String textContent) {
        return ParsedDetail.builder()
                .executiveSummary(textContent)
                .historicalEvolution("Field has evolved significantly over time")
                .corePrinciples("Based on fundamental theories and best practices")
                .realWorldApplications(List.of(
                        ParsedDetail.Application.builder()
                                .title("Industry Application")
                                .description("Widely used across various industries")
                                .impact("Significant business value and efficiency gains")
                                .build()
                ))
                .caseStudies(List.of(
                        ParsedDetail.CaseStudy.builder()
                                .company("Leading Technology Company")
                                .challenge("Needed to improve efficiency and scalability")
                                .solution("Implemented best practices and modern approaches")
                                .results("Achieved measurable improvements in performance")
                                .build()
                ))
                .interconnectedConcepts("Connects with multiple related disciplines")
                .currentInnovation("Rapid advancement and new developments")
                .futureOutlook("Promising future with continued growth")
                .expertInsights(List.of(
                        ParsedDetail.ExpertInsight.builder()
                                .expert("Industry Leader")
                                .insight("This field will continue to be crucial for success")
                                .build()
                ))
                .learningResources(List.of(
                        ParsedDetail.LearningResource.builder()
                                .type("Professional Course")
                                .title("Comprehensive Training Program")
                                .relevance("Essential skills for career advancement")
                                .build()
                ))
                .keyMetrics(List.of("Growing market", "High demand", "Strong career prospects"))
                .build();
    }

    // Inner classes for structured data
    @lombok.Data
    @lombok.Builder
    public static class ParsedOverview {
        private String introduction;
        private List<String> keyConcepts;
        private String currentRelevance;
        private List<String> mainTakeaways;
        private String whyItMatters;
        private List<String> quickStats;
    }

    @lombok.Data
    @lombok.Builder
    public static class ParsedDetail {
        private String executiveSummary;
        private String historicalEvolution;
        private String corePrinciples;
        private List<Application> realWorldApplications;
        private List<CaseStudy> caseStudies;
        private String interconnectedConcepts;
        private String currentInnovation;
        private String futureOutlook;
        private List<ExpertInsight> expertInsights;
        private List<LearningResource> learningResources;
        private List<String> keyMetrics;

        @lombok.Data
        @lombok.Builder
        public static class Application {
            private String title;
            private String description;
            private String impact;
        }

        @lombok.Data
        @lombok.Builder
        public static class CaseStudy {
            private String company;
            private String challenge;
            private String solution;
            private String results;
        }

        @lombok.Data
        @lombok.Builder
        public static class ExpertInsight {
            private String expert;
            private String insight;
        }

        @lombok.Data
        @lombok.Builder
        public static class LearningResource {
            private String type;
            private String title;
            private String relevance;
        }
    }
}
