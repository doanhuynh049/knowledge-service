package com.knowledge.stock.service;

import com.knowledge.stock.model.StockLearningDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockContentGenerationService {

    private final RestTemplate restTemplate;

    @Value("${app.llm-api-key}")
    private String geminiApiKey;

    @Value("${app.llm-provider}")
    private String geminiUrl;

    /**
     * Generate AI prompt for a stock knowledge topic
     */
    public String generateStockPrompt(String topic) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a financial expert and stock market analyst. ");
        prompt.append("Please provide comprehensive educational content about: ").append(topic).append("\n\n");
        
        prompt.append("Please structure your response as follows:\n");
        prompt.append("1. **Definition & Overview**: Clear explanation of the concept\n");
        prompt.append("2. **Key Principles**: Main principles and rules to remember\n");
        prompt.append("3. **Practical Examples**: Real-world examples with specific stocks or scenarios\n");
        prompt.append("4. **Common Mistakes**: What investors typically get wrong\n");
        prompt.append("5. **Implementation Tips**: Step-by-step guidance for beginners\n");
        prompt.append("6. **Risk Considerations**: Important risks and how to mitigate them\n");
        prompt.append("7. **Further Learning**: Resources or next steps for deeper understanding\n\n");
        
        prompt.append("Make the content educational, practical, and suitable for both beginners and intermediate investors. ");
        prompt.append("Use clear language and avoid overly technical jargon. ");
        prompt.append("Include specific examples where possible to illustrate concepts.");

        return prompt.toString();
    }

    /**
     * Generate prompt for weekly stock report
     */
    public String generateWeeklyStockReport(List<String> allTopics) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a financial expert creating a weekly stock market knowledge summary. ");
        prompt.append("Based on these key investment topics: ");
        
        for (int i = 0; i < allTopics.size(); i++) {
            prompt.append((i + 1)).append(". ").append(allTopics.get(i));
            if (i < allTopics.size() - 1) {
                prompt.append(", ");
            }
        }
        
        prompt.append("\n\nPlease create a comprehensive weekly market outlook that includes:\n");
        prompt.append("1. **Market Overview**: Current market sentiment and trends\n");
        prompt.append("2. **Key Focus Areas**: Which of the above topics are most relevant this week\n");
        prompt.append("3. **Investment Opportunities**: Sectors or strategies to consider\n");
        prompt.append("4. **Risk Alerts**: Current market risks and concerns\n");
        prompt.append("5. **Weekly Action Items**: Specific steps investors should take\n");
        prompt.append("6. **Educational Highlights**: Key learning points from the topics\n\n");
        
        prompt.append("Make this practical and actionable for investors of all levels.");

        try {
            return getAIResponse(prompt.toString());
        } catch (Exception e) {
            log.error("Error generating weekly stock report: {}", e.getMessage(), e);
            return generateFallbackWeeklyReport(allTopics);
        }
    }

    /**
     * Get AI response from Gemini API
     */
    public String getAIResponse(String prompt) {
        log.info("Requesting AI response for prompt length: {} characters", prompt.length());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            
            // Create contents array
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 2048);
            requestBody.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(geminiUrl, request, Map.class);

            if (response.getBody() != null) {
                return extractTextFromGeminiResponse(response.getBody());
            } else {
                throw new RuntimeException("Empty response from Gemini API");
            }

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return generateFallbackContent(prompt);
        }
    }

    /**
     * Extract text from Gemini API response
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromGeminiResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            throw new RuntimeException("Could not extract text from Gemini response");
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing Gemini response: " + e.getMessage());
        }
    }

    /**
     * Generate fallback content when AI is unavailable
     */
    private String generateFallbackContent(String prompt) {
        log.warn("Generating fallback content due to AI service unavailability");
        
        return """
            **Stock Knowledge Topic - Educational Content**
            
            We apologize, but our AI content generation service is currently unavailable.
            This is a placeholder educational content for the requested stock market topic.
            
            **Important Note**: Please refer to reliable financial resources, consult with
            financial advisors, and conduct thorough research before making any investment decisions.
            
            **Key Reminders**:
            - Always do your own research
            - Diversify your portfolio
            - Understand the risks involved
            - Consider your risk tolerance
            - Invest only what you can afford to lose
            
            For comprehensive information on this topic, please refer to:
            - SEC.gov investor resources
            - Reputable financial news sources
            - Licensed financial advisors
            - Educational investment platforms
            
            This content will be updated once our AI service is restored.
            """;
    }

    /**
     * Generate fallback weekly report
     */
    private String generateFallbackWeeklyReport(List<String> topics) {
        StringBuilder report = new StringBuilder();
        
        report.append("**Weekly Stock Market Knowledge Summary**\n\n");
        report.append("*Note: AI service temporarily unavailable - Manual summary provided*\n\n");
        
        report.append("**Topics Covered This Week**:\n");
        for (int i = 0; i < topics.size(); i++) {
            report.append((i + 1)).append(". ").append(topics.get(i)).append("\n");
        }
        
        report.append("\n**General Market Guidance**:\n");
        report.append("- Stay informed about market trends and economic indicators\n");
        report.append("- Maintain a diversified investment portfolio\n");
        report.append("- Review and rebalance your portfolio regularly\n");
        report.append("- Continue learning about investment strategies\n");
        report.append("- Consult with financial professionals when needed\n\n");
        
        report.append("**Risk Management Reminder**:\n");
        report.append("- Never invest more than you can afford to lose\n");
        report.append("- Understand the risks of any investment before committing\n");
        report.append("- Keep emergency funds separate from investment capital\n");
        report.append("- Consider your time horizon and risk tolerance\n\n");
        
        report.append("For detailed analysis and current market conditions, please refer to ");
        report.append("reputable financial news sources and professional advisors.\n");
        
        return report.toString();
    }

    /**
     * Generate structured prompt for specific learning day
     */
    public String generateStructuredStockPrompt(StockLearningDay learningDay) {
        log.info("🧠 Generating structured prompt for Day {}: {}", learningDay.getDay(), learningDay.getTopic());

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a professional stock market educator creating content for Day ")
              .append(learningDay.getDay()).append(" of a structured learning curriculum.\n\n");

        prompt.append("**Learning Context:**\n");
        prompt.append("- Phase: ").append(learningDay.getPhase()).append("\n");
        prompt.append("- Week: ").append(learningDay.getWeek()).append("\n");
        prompt.append("- Topic: ").append(learningDay.getTopic()).append("\n");
        prompt.append("- Learning Goal: ").append(learningDay.getLearningGoal()).append("\n\n");

        prompt.append("**Content Requirements:**\n");
        prompt.append("Create comprehensive educational content about: ").append(learningDay.getTopic()).append("\n\n");

        prompt.append("Structure your response with these sections:\n");
        prompt.append("1. **Introduction & Why This Matters**: Brief overview and importance\n");
        prompt.append("2. **Core Concepts**: Key principles and definitions\n");
        prompt.append("3. **Practical Examples**: Real-world applications with VN and US market examples\n");
        prompt.append("4. **Step-by-Step Guide**: How to apply this knowledge\n");
        prompt.append("5. **Common Mistakes**: What beginners should avoid\n");
        prompt.append("6. **Quick Reference**: Key points summary for future reference\n\n");

        prompt.append("**Important Guidelines:**\n");
        prompt.append("- Write for beginners who are serious about learning\n");
        prompt.append("- Include specific examples from both Vietnamese (VN-Index) and US markets (S&P500, NASDAQ)\n");
        prompt.append("- Make it actionable - students should be able to immediately apply this knowledge\n");
        prompt.append("- Keep language clear but professional\n");
        prompt.append("- Focus on practical application rather than just theory\n\n");

        prompt.append("**Practice Task Context:**\n");
        prompt.append("Students will: ").append(learningDay.getPracticeTask()).append("\n\n");

        prompt.append("Ensure your content directly supports this practice task and provides the knowledge needed to complete it successfully.");

        log.debug("📝 Generated structured prompt ({} characters) for Day {}", prompt.length(), learningDay.getDay());
        return prompt.toString();
    }
}
