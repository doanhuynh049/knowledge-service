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
                return "";
            }

        } catch (Exception e) {
            log.error("Error generating {} content for topic {}: {}", contentType, topicName, e.getMessage(), e);
            return "";
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
      String topicName = topic.getName();
      String category = topic.getCategory();

      return String.format("""
          You are an expert programming mentor. I want to learn about the topic: **%s** (Category: %s).
          Provide a comprehensive, well-structured explanation suitable for learning or reference.

          **IMPORTANT: Format your entire response strictly as valid JSON** with the following structure for easy parsing and HTML conversion. 
          Do not include any extra text outside of JSON.

          {
            "overview": {
              "definition": "Concise definition of %s with technical accuracy",
              "importance": "Why this topic is crucial for developers and its practical applications",
              "historicalContext": "Evolution, key milestones, and why it became important"
            },
            "keyConcepts": [
              {"title": "Core Concept 1", "description": "Detailed explanation with terminology and definitions", "example": "Simple practical example or use case"},
              {"title": "Core Concept 2", "description": "Technical details and how it works", "example": "Code snippet or implementation detail"},
              {"title": "Core Concept 3", "description": "Advanced aspect or component explanation", "example": "Real-world application or pattern"},
              {"title": "Core Concept 4", "description": "Related principle or methodology", "example": "Best practice or common usage"},
              {"title": "Core Concept 5", "description": "Performance or optimization aspect", "example": "Efficiency consideration or technique"}
            ],
            "codeExamples": [
              {"title": "Basic Example - Getting Started", "language": "java", "code": "// Clear, commented code example\\nclass Example {\\n    public void demonstrate() {\\n        // Step-by-step implementation\\n    }\\n}", "explanation": "What this code does and why it's structured this way"},
              {"title": "Intermediate Example - Common Pattern", "language": "java", "code": "// More complex usage with real-world scenario", "explanation": "Advanced features and practical considerations"},
              {"title": "Advanced Example - Production Ready", "language": "java", "code": "// Professional implementation with performance optimizations", "explanation": "Production-level considerations and optimizations"}
            ],
            "comparisons": {
              "alternatives": [
                {"name": "Alternative 1", "differences": "Key differences and when to use each", "advantages": "Benefits of %s over this alternative", "disadvantages": "When the alternative might be better"},
                {"name": "Alternative 2", "differences": "Technical comparison and use cases", "advantages": "Strengths of %s in comparison", "disadvantages": "Limitations or trade-offs"}
              ],
              "summary": "Overall positioning of %s in the technology landscape"
            },
            "bestPractices": {
              "dos": ["Essential practice 1", "Important guideline 2", "Security/performance consideration 3", "Code quality practice 4"],
              "donts": ["Common mistake 1", "Performance pitfall 2", "Security issue 3", "Design anti-pattern 4"],
              "optimizationTips": ["Performance optimization 1", "Memory efficiency tip 2", "Scalability consideration 3", "Debugging and maintenance tip 4"]
            },
            "advancedTechniques": {
              "specializedScenarios": [
                {"scenario": "Enterprise Application", "techniques": "Advanced patterns and architecture", "realWorldExample": "Company/project that implements this successfully"},
                {"scenario": "High-Performance Requirements", "techniques": "Optimization strategies and performance tuning", "realWorldExample": "Performance-critical use case"}
              ],
              "caseStudies": [
                {"company": "Tech Company", "challenge": "Specific technical problem", "solution": "How %s solved it", "results": "Quantified outcomes and lessons learned"}
              ]
            },
            "relatedTopics": [
              {"topic": "Related Concept 1", "connection": "How it relates to %s", "learningOrder": "Before/after/alongside"},
              {"topic": "Related Concept 2", "connection": "Technical relationship and practical overlap", "learningOrder": "Recommended sequence"},
              {"topic": "Related Concept 3", "connection": "Complementary skills and knowledge areas", "learningOrder": "Strategic learning path guidance"}
            ],
            "learningResources": [
              {"type": "Official Documentation", "title": "Primary documentation", "relevance": "Essential reference", "level": "All levels"},
              {"type": "Tutorial Series", "title": "Comprehensive course", "relevance": "Hands-on practice", "level": "Beginner to Intermediate"},
              {"type": "Advanced Book", "title": "In-depth book by expert", "relevance": "Advanced patterns", "level": "Intermediate to Advanced"},
              {"type": "Community Resource", "title": "Forum or repo", "relevance": "Real-world examples", "level": "All levels"},
              {"type": "Video Course", "title": "High-quality tutorial", "relevance": "Visual learning", "level": "Beginner to Advanced"}
            ],
            "keyMetrics": ["Learning time: X weeks", "Industry adoption: X%%", "Job market: X,000+ positions", "Salary impact: $X premium"]
          }

          **Instructions for AI Output:**
          - Output must be strictly valid JSON. No extra commentary outside JSON.
          - Include all sections and nested objects as specified.
          - Use real Java code examples with proper syntax and comments.
          - Include actionable best practices, performance tips, and industry-relevant references.
          - Word count target: 1500-2500 words across all sections.
          """, topicName, category, topicName, topicName, topicName, topicName, topicName, topicName, topicName);
  }
}
