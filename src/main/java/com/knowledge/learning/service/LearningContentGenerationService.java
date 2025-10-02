package com.knowledge.learning.service;

import com.knowledge.learning.model.LearningDay;
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
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningContentGenerationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.gemini-api-key}")
    private String geminiApiKey;

    @Value("${app.gemini-api-url}")
    private String geminiApiUrl;

    /**
     * Generate AI content for a specific learning day
     */
    public String getAIResponse(String prompt) {
        log.info("🤖 Requesting AI response for learning content...");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            
            // Create contents array
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});

            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 50000);
            requestBody.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("🌐 Sending request to Gemini API...");
            ResponseEntity<Map> response = restTemplate.postForEntity(geminiApiUrl, request, Map.class);

            if (response.getBody() != null) {
                // Extract text from response
                Map<String, Object> responseBody = response.getBody();
                Object candidates = responseBody.get("candidates");
                
                if (candidates instanceof java.util.List) {
                    java.util.List<Map<String, Object>> candidatesList = (java.util.List<Map<String, Object>>) candidates;
                    if (!candidatesList.isEmpty()) {
                        Map<String, Object> firstCandidate = candidatesList.get(0);
                        Map<String, Object> content1 = (Map<String, Object>) firstCandidate.get("content");
                        java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content1.get("parts");
                        if (!parts.isEmpty()) {
                            String aiText = (String) parts.get(0).get("text");
                            log.info("✅ AI response received ({} characters)", aiText.length());
                            return aiText;
                        }
                    }
                }
            }

            log.warn("⚠️ Empty or invalid AI response received");
            return generateFallbackContent();

        } catch (Exception e) {
            log.error("❌ Error calling Gemini API: {}", e.getMessage(), e);
            return generateFallbackContent();
        }
    }

    /**
     * Generate structured prompt for specific learning day with JSON output format
     */
    public String generateStructuredLearningPrompt(LearningDay learningDay) {
        log.info("🧠 Generating structured prompt for Day {}: {}", learningDay.getDay(), learningDay.getPhase());

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a professional software development educator creating content for Day ")
            .append(learningDay.getDay())
            .append(" of a 6-month structured learning curriculum.\n\n");

        prompt.append("**Learning Context:**\n");
        prompt.append("- Phase: ").append(learningDay.getPhase()).append("\n");
        prompt.append("- Week: ").append(learningDay.getWeek()).append("\n");
        prompt.append("- Day: ").append(learningDay.getDay()).append("\n");
        prompt.append("- Algorithm Task: ").append(learningDay.getAlgorithmTask()).append("\n");
        prompt.append("- Theory Task: ").append(learningDay.getTheoryTask()).append("\n");
        prompt.append("- Coding Task: ").append(learningDay.getCodingTask()).append("\n");
        prompt.append("- Reflection Task: ").append(learningDay.getReflectionTask()).append("\n\n");

        // 🔴 Key requirement: Generate complete HTML document
        prompt.append("Create a comprehensive HTML learning guide document (complete HTML with DOCTYPE, head, body).\n");
        prompt.append("Use semantic HTML structure with proper sections and styling.\n");
        prompt.append("Include internal CSS for professional formatting and layout.\n");
        prompt.append("Use clean, educational design with good typography and spacing.\n");
        prompt.append("Make it suitable for email viewing with responsive design.\n\n");

        prompt.append("Structure the HTML document with these main sections:\n");
        prompt.append("1. Header with day title and learning phase\n");
        prompt.append("2. Daily Overview section\n");
        prompt.append("3. Algorithm Practice Guide section\n");
        prompt.append("4. Theory Deep Dive section\n");
        prompt.append("5. Hands-on Coding Guide section\n");
        prompt.append("6. Reflection & Documentation section\n");
        prompt.append("7. Resources & Tomorrow's Prep section\n\n");

        prompt.append("Use the following HTML structure template:\n");
        prompt.append("<!DOCTYPE html>\n");
        prompt.append("<html lang=\"en\">\n");
        prompt.append("<head>\n");
        prompt.append("    <meta charset=\"UTF-8\">\n");
        prompt.append("    <title>Day ").append(learningDay.getDay()).append(": ").append(learningDay.getPhase()).append("</title>\n");
        prompt.append("    <style>/* Include comprehensive CSS styling */</style>\n");
        prompt.append("</head>\n");
        prompt.append("<body>\n");
        prompt.append("    <main>\n");
        prompt.append("        <section class=\"block\">/* Content sections */</section>\n");
        prompt.append("    </main>\n");
        prompt.append("</body>\n");
        prompt.append("</html>\n\n");

        prompt.append("**Content Requirements for: ").append(learningDay.getPhase()).append("**\n\n");
        prompt.append("**Guidelines for each section:**\n");
        prompt.append("- **Daily Overview**: 2-3 paragraphs on learning structure and time management\n");
        prompt.append("- **Algorithm Guidance**: Specific problem-solving approaches and practice strategies\n");
        prompt.append("- **Theory Explanation**: Clear explanations with examples and real-world applications\n");
        prompt.append("- **Coding Exercises**: Practical step-by-step coding tasks with explanations\n");
        prompt.append("- **Reflection Prompts**: Questions to guide learning reflection and documentation\n");
        prompt.append("- **Resources**: Links to documentation, tutorials, and tomorrow's preparation\n\n");

        prompt.append("Make all content practical, actionable, and appropriate for a ")
            .append(learningDay.getPhase()).append(" learning phase. ");
        prompt.append("Focus on building skills progressively and maintaining motivation throughout the 6-month journey.\n\n");
        prompt.append("Include specific examples, code snippets where appropriate, and emphasize best practices for software development learning.\n");
        prompt.append("Output the complete HTML document only (no additional text before or after).\n");

        log.info("📝 Generated prompt for Day {} ({} characters)", learningDay.getDay(), prompt.length());
        return prompt.toString();
    }

    /**
     * Generate fallback content when AI service is unavailable
     */
    private String generateFallbackContent() {
        log.warn("🔄 Generating fallback learning content...");

        StringBuilder fallback = new StringBuilder();
        
        fallback.append("**Daily Learning Path**\n\n");
        fallback.append("*Note: AI service temporarily unavailable - Basic guidance provided*\n\n");
        
        fallback.append("**Today's Learning Structure**:\n");
        fallback.append("- 30 min: Algorithm/Data Structure practice\n");
        fallback.append("- 30 min: Theory study and reading\n");
        fallback.append("- 30-60 min: Hands-on coding practice\n");
        fallback.append("- 15 min: Reflection and documentation\n\n");
        
        fallback.append("**General Learning Guidelines**:\n");
        fallback.append("- Break down complex problems into smaller parts\n");
        fallback.append("- Practice coding daily to build muscle memory\n");
        fallback.append("- Read documentation and understand fundamentals\n");
        fallback.append("- Document your learning process and insights\n");
        fallback.append("- Review and refactor your code regularly\n\n");
        
        fallback.append("**Recommended Resources**:\n");
        fallback.append("- Official language documentation\n");
        fallback.append("- Online coding platforms for practice\n");
        fallback.append("- Technical blogs and tutorials\n");
        fallback.append("- Open source projects for reference\n\n");
        
        fallback.append("For detailed guidance, please try again when the AI service is available.\n");
        
        return fallback.toString();
    }
}
