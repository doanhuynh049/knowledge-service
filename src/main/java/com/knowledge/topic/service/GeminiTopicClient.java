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

    public String generateDetailedContent(Topic topic) {
        String prompt = buildDetailedPrompt(topic);
        return generateContent(prompt, "detailed", topic.getName());
    }

    private String generateContent(String prompt, String contentType, String topicName) {
        log.info("Generating {} content for topic: {}", contentType, topicName);

        try {
            GeminiRequest request = GeminiRequest.create(prompt);

            GeminiResponse response = webClient.post()
                    .uri("?key=" + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException &&
                                    ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (response != null && response.getGeneratedText() != null) {
                String generatedText = response.getGeneratedText();
                log.info("Successfully generated {} content for {}: {} words",
                        contentType, topicName, generatedText.split("\\s+").length);
                return generatedText;
            } else {
                log.error("Empty response from Gemini API for {} content of topic: {}", contentType, topicName);
                return generateFallbackContent(topicName, contentType);
            }

        } catch (Exception e) {
            log.error("Error generating {} content for topic {}: {}", contentType, topicName, e.getMessage(), e);
            return generateFallbackContent(topicName, contentType);
        }
    }

    private String buildOverviewPrompt(Topic topic) {
        return String.format("""
                Generate a comprehensive but concise overview for the topic: "%s"
                Category: %s
                
                Please provide:
                1. Introduction (2-3 paragraphs explaining what this topic is and why it matters)
                2. Key Concepts (3-5 main concepts that define this topic)
                3. Current Relevance (how this topic applies in today's world)
                4. Main Takeaways (3-5 bullet points summarizing the most important points)
                5. Why It Matters (practical importance and real-world impact)
                
                Format: Professional, engaging, suitable for busy professionals
                Length: 300-500 words
                Tone: Educational but accessible
                
                Focus on clarity and practical understanding rather than technical jargon.
                """, topic.getName(), topic.getCategory());
    }

    private String buildDetailedPrompt(Topic topic) {
        return String.format("""
                Generate comprehensive detailed knowledge content for the topic: "%s"
                Category: %s
                
                Please provide an in-depth exploration covering:
                1. Comprehensive Introduction (2 paragraphs with context and significance)
                2. Historical Context (evolution and development over time)
                3. Core Mechanisms/Principles (how it works, underlying theories)
                4. Technical Details (specific processes, methodologies, or systems)
                5. Real-World Applications (3-5 concrete examples with explanations)
                6. Case Studies (1-2 detailed examples of implementation/impact)
                7. Related Concepts (interconnections with other topics)
                8. Current Trends (recent developments and innovations)
                9. Future Outlook (predictions and emerging directions)
                10. Expert Insights (notable quotes or perspectives from thought leaders)
                11. Further Learning (3-5 recommended resources for deeper study)
                
                Format: Academic yet accessible, suitable for serious learners
                Length: 1000-1500 words
                Tone: Authoritative, comprehensive, engaging
                Include relevant statistics, dates, and specific examples where applicable.
                Ensure each section flows naturally into the next.
                """, topic.getName(), topic.getCategory());
    }

    private String generateFallbackContent(String topicName, String contentType) {
        if ("overview".equals(contentType)) {
            return String.format("""
                    # %s - Topic Overview
                    
                    This is an important topic that deserves careful study and understanding. 
                    While we're currently unable to generate detailed content, this topic 
                    remains highly relevant in today's context.
                    
                    ## Key Points:
                    • Significant impact on current industry trends
                    • Important for professional development
                    • Relevant to future technological advancement
                    • Essential for comprehensive understanding
                    
                    ## Why It Matters:
                    Understanding this topic provides valuable insights that can enhance 
                    decision-making and strategic thinking in relevant fields.
                    
                    Please check back later for more comprehensive content.
                    """, topicName);
        } else {
            return String.format("""
                    # %s - Comprehensive Analysis
                    
                    ## Introduction
                    This topic represents an important area of study with significant implications 
                    for both theoretical understanding and practical application.
                    
                    ## Historical Context
                    The development of this field has been shaped by various factors and 
                    continues to evolve based on new discoveries and applications.
                    
                    ## Core Principles
                    The fundamental concepts underlying this topic provide a framework 
                    for understanding its broader implications and applications.
                    
                    ## Real-World Applications
                    • Industrial applications and implementations
                    • Academic research and theoretical development
                    • Practical solutions to real-world problems
                    
                    ## Future Outlook
                    Continued development in this area promises exciting opportunities 
                    for innovation and advancement.
                    
                    ## Further Learning
                    • Academic journals and research publications
                    • Industry reports and case studies  
                    • Professional development resources
                    
                    Please check back later for more detailed content.
                    """, topicName);
        }
    }
}
