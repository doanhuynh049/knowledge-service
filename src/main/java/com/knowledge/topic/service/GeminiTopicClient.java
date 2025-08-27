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
                Generate a comprehensive overview for the topic: "%s" (Category: %s)
                
                **IMPORTANT: Structure your response in the following JSON-like format for easy parsing:**
                
                {
                  "introduction": "2-3 paragraph introduction with clear definition, importance, and recent developments",
                  "keyConcepts": [
                    "Core principle 1 with brief explanation",
                    "Core principle 2 with brief explanation", 
                    "Core principle 3 with brief explanation",
                    "Core principle 4 with brief explanation"
                  ],
                  "currentRelevance": "1-2 paragraphs on modern impact, trends, and applications",
                  "mainTakeaways": [
                    "Actionable insight 1 with practical value",
                    "Actionable insight 2 with key facts",
                    "Actionable insight 3 with decision-making guidance",
                    "Actionable insight 4 with professional relevance"
                  ],
                  "whyItMatters": "1 paragraph on practical importance, career relevance, and future opportunities",
                  "quickStats": [
                    "Relevant statistic or metric 1",
                    "Industry growth/adoption figure",
                    "Market size or impact measure"
                  ]
                }
                
                **Content Guidelines:**
                - Total length: 400-600 words
                - Include specific examples and real companies where relevant
                - Use current data and trends (2024-2025)
                - Make it immediately actionable for professionals
                - Focus on practical applications and career benefits
                - Keep language engaging but professional
                
                **Format the final output as valid JSON that can be easily parsed.**
                """, topic.getName(), topic.getCategory());
    }

    private String buildDetailedPrompt(Topic topic) {
        return String.format("""
                Create comprehensive expert-level content for: "%s" (Category: %s)
                
                **IMPORTANT: Structure your response in the following JSON format for easy parsing:**
                
                {
                  "executiveSummary": "2 paragraphs covering strategic importance and overview",
                  "historicalEvolution": "2-3 paragraphs on key milestones and paradigm shifts",
                  "corePrinciples": "3-4 paragraphs on fundamental theories and technical details",
                  "realWorldApplications": [
                    {
                      "title": "Application 1 Title",
                      "description": "Detailed description with company/industry example",
                      "impact": "Quantifiable results or benefits"
                    },
                    {
                      "title": "Application 2 Title", 
                      "description": "Detailed description with specific implementation",
                      "impact": "Success metrics and outcomes"
                    },
                    {
                      "title": "Application 3 Title",
                      "description": "Innovation use case with real example", 
                      "impact": "Business value and transformation"
                    }
                  ],
                  "caseStudies": [
                    {
                      "company": "Real Company/Organization Name",
                      "challenge": "Specific problem they faced",
                      "solution": "How they applied this topic",
                      "results": "Quantified outcomes and lessons learned"
                    },
                    {
                      "company": "Another Real Example",
                      "challenge": "Different challenge scenario",
                      "solution": "Implementation approach used",
                      "results": "Measurable impact and best practices"
                    }
                  ],
                  "interconnectedConcepts": "2 paragraphs on related fields and cross-disciplinary connections",
                  "currentInnovation": "2-3 paragraphs on latest research, tools, and industry disruptions",
                  "futureOutlook": "2 paragraphs on 5-10 year predictions and strategic implications",
                  "expertInsights": [
                    {
                      "expert": "Industry Leader Name/Title",
                      "insight": "Notable quote or perspective on the topic"
                    },
                    {
                      "expert": "Research Authority/Institution", 
                      "insight": "Key research finding or contrasting viewpoint"
                    }
                  ],
                  "learningResources": [
                    {
                      "type": "Academic Journal",
                      "title": "Specific journal or paper title",
                      "relevance": "Why this resource is valuable"
                    },
                    {
                      "type": "Professional Course",
                      "title": "Certification or training program",
                      "relevance": "Skills and credentials gained"
                    },
                    {
                      "type": "Industry Report",
                      "title": "Research report or whitepaper",
                      "relevance": "Market insights and trends"
                    },
                    {
                      "type": "Conference/Community",
                      "title": "Key event or professional network",
                      "relevance": "Networking and knowledge sharing opportunities"
                    },
                    {
                      "type": "Essential Reading",
                      "title": "Book by recognized expert",
                      "relevance": "Foundational knowledge and advanced concepts"
                    }
                  ],
                  "keyMetrics": [
                    "Market size: $X billion industry",
                    "Growth rate: X% annually", 
                    "Job market: X positions available",
                    "Salary impact: X% premium for expertise"
                  ]
                }
                
                **Content Requirements:**
                - Total length: 1200-1800 words
                - Include real companies, products, and specific implementations
                - Use current data and statistics (2024-2025)
                - Reference authoritative sources and research
                - Balance theoretical depth with practical applications
                - Provide actionable insights for different professional levels
                
                **Format the final output as valid JSON that can be easily parsed and converted to HTML.**
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
