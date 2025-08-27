package com.knowledge.topic.service;

import com.knowledge.topic.dto.GeminiRequest;
import com.knowledge.topic.dto.GeminiResponse;
import com.knowledge.topic.model.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class GeminiTopicClient {

    private final WebClient webClient;
    private final String apiKey;

    public GeminiTopicClient(@Value("${app.llm-provider}") String apiUrl,
                           @Value("${app.llm-api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String generateOverviewContent(Topic topic) {
        String prompt = buildOverviewPrompt(topic);
        return generateContent(prompt, "overview", topic.getName());
    }



    private String generateContent(String prompt, String contentType, String topicName) {
        log.info("Generating {} content for topic: {}", contentType, topicName);
        log.debug("Using prompt: {}", prompt.substring(0, Math.min(prompt.length(), 200)) + "...");

        try {
            GeminiRequest request = GeminiRequest.create(prompt);

            GeminiResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException &&
                                    ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                    .timeout(Duration.ofSeconds(120))
                    .block();

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                String content = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                
                // Log content length for monitoring
                log.info("Generated {} content for '{}': {} characters", contentType, topicName, content.length());
                log.debug("Generated content preview: {}", content.substring(0, Math.min(content.length(), 300)));
                
                // Check for potential truncation issues
                if (content.length() > 8000) {
                    log.warn("Generated content is very long ({}+ chars) - potential truncation risk for topic: {}", 
                            content.length(), topicName);
                }
                
                return content;
            }

            log.warn("Empty response received for {} content generation for topic: {}", contentType, topicName);
            log.info("Falling back to generated fallback content for topic: {}", topicName);
            return generateFallbackContent(topicName, contentType);

        } catch (Exception e) {
            log.error("Error generating {} content for topic: {} - {}", contentType, topicName, e.getMessage());
            log.info("Falling back to generated fallback content for topic: {}", topicName);
            return generateFallbackContent(topicName, contentType);
        }
    }

    private String generateFallbackContent(String topicName, String contentType) {
        // Only support overview content fallback
        return String.format("""
            {
              "introduction": "%s is a crucial technology in modern software development that enables developers to build robust, scalable applications with industry-proven patterns and practices.",
              "keyConcepts": [
                "Core architectural patterns and design principles specific to %s",
                "Implementation strategies and development methodologies for %s", 
                "Industry best practices and proven approaches for %s applications",
                "Integration patterns and ecosystem compatibility considerations"
              ],
              "currentRelevance": "In today's rapidly evolving tech landscape, %s remains highly relevant with continuous updates, strong community support, and widespread enterprise adoption across industries.",
              "mainTakeaways": [
                "Mastering %s fundamentals is essential for building maintainable applications",
                "Following established %s patterns ensures code quality and team collaboration",
                "Staying current with %s developments is crucial for professional growth",
                "Hands-on practice with %s accelerates practical understanding and expertise"
              ],
              "whyItMatters": "Proficiency in %s provides developers with powerful capabilities for creating enterprise-grade solutions, enhances career prospects, and enables contribution to high-impact projects in modern software development.",
              "quickStats": [
                "High demand in enterprise and startup environments",
                "Essential skill for full-stack and backend development",
                "Continuously evolving with new features and improvements"
              ]
            }
            """, topicName, topicName, topicName, topicName, topicName, topicName, topicName, topicName, topicName, topicName);
    }

    private String buildOverviewPrompt(Topic topic) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Programming Topic Overview Generation\n");
        prompt.append(String.format("Create a comprehensive overview for: %s (%s)\n\n", topic.getName(), topic.getCategory()));
        
        prompt.append("Content Requirements:\n");
        prompt.append("- Professional, developer-focused content\n");
        prompt.append("- Include current industry relevance and trends\n");
        prompt.append("- Provide actionable insights and practical value\n");
        prompt.append("- Keep content between 400-600 words total\n");
        prompt.append("- Focus on career benefits and real-world applications\n\n");
        
        prompt.append("Output Format - Return ONLY valid JSON in this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"introduction\": \"2-3 paragraph introduction with clear definition, importance, and recent developments\",\n");
        prompt.append("  \"keyConcepts\": [\n");
        prompt.append("    \"Core principle 1 with brief explanation\",\n");
        prompt.append("    \"Core principle 2 with brief explanation\",\n");
        prompt.append("    \"Core principle 3 with brief explanation\",\n");
        prompt.append("    \"Core principle 4 with brief explanation\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"currentRelevance\": \"1-2 paragraphs on modern impact, trends, and applications\",\n");
        prompt.append("  \"mainTakeaways\": [\n");
        prompt.append("    \"Actionable insight 1 with practical value\",\n");
        prompt.append("    \"Actionable insight 2 with key facts\",\n");
        prompt.append("    \"Actionable insight 3 with decision-making guidance\",\n");
        prompt.append("    \"Actionable insight 4 with professional relevance\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"whyItMatters\": \"1 paragraph on practical importance, career relevance, and future opportunities\",\n");
        prompt.append("  \"quickStats\": [\n");
        prompt.append("    \"Relevant statistic or metric 1\",\n");
        prompt.append("    \"Industry growth/adoption figure\",\n");
        prompt.append("    \"Market size or impact measure\"\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        
        prompt.append("CRITICAL INSTRUCTIONS:\n");
        prompt.append("- Return ONLY the JSON object - no markdown blocks, no additional text\n");
        prompt.append("- Include specific examples and real companies where relevant\n");
        prompt.append("- Use current data and trends (2024-2025)\n");
        prompt.append("- Make content immediately actionable for professionals\n");
        prompt.append("- Keep language engaging but professional\n");
        prompt.append("- Ensure valid JSON syntax with proper quotes and brackets\n");
        
        return prompt.toString();
    }
}
