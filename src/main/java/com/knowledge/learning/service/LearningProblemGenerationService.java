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
public class LearningProblemGenerationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.gemini-api-key}")
    private String geminiApiKey;

    @Value("${app.gemini-api-url}")
    private String geminiApiUrl;

    /**
     * Generate problem-based learning content for specific learning day
     */
    public String generateProblemContent(LearningDay learningDay) {
        log.info("🎯 Generating problem-based content for Day {}: {}", learningDay.getDay(), learningDay.getPhase());

        try {
            String prompt = generateProblemPrompt(learningDay);
            String aiResponse = getAIResponse(prompt);
            
            log.info("✅ Problem content generated for Day {} ({} characters)", 
                learningDay.getDay(), aiResponse.length());
            
            return aiResponse;

        } catch (Exception e) {
            log.error("❌ Error generating problem content for Day {}: {}", learningDay.getDay(), e.getMessage(), e);
            return generateFallbackProblemContent(learningDay);
        }
    }

    /**
     * Generate AI prompt for problem-based learning
     */
    private String generateProblemPrompt(LearningDay learningDay) {
        log.info("🧠 Creating problem-based prompt for Day {}: {}", learningDay.getDay(), learningDay.getPhase());

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a professional software development educator creating problem-based learning challenges for Day ")
            .append(learningDay.getDay())
            .append(" of a 6-month structured learning curriculum.\n\n");

        prompt.append("**Learning Context:**\n");
        prompt.append("- Phase: ").append(learningDay.getPhase()).append("\n");
        prompt.append("- Day: ").append(learningDay.getDay()).append("\n");
        prompt.append("- Algorithm Focus: ").append(learningDay.getAlgorithmTask()).append("\n");
        prompt.append("- Theory Focus: ").append(learningDay.getTheoryTask()).append("\n");
        prompt.append("- Coding Focus: ").append(learningDay.getCodingTask()).append("\n");
        prompt.append("- Reflection Focus: ").append(learningDay.getReflectionTask()).append("\n\n");

        prompt.append("**Task: Create 3 Progressive Programming Problems**\n");
        prompt.append("Generate exactly 3 problems of increasing difficulty based on today's learning topics:\n");
        prompt.append("1. **EASY Problem** - Beginner level, focuses on basic understanding\n");
        prompt.append("2. **MEDIUM Problem** - Intermediate level, requires combining concepts\n");
        prompt.append("3. **HARD Problem** - Advanced level, requires creative problem-solving\n\n");

        prompt.append("**Output Format: Complete HTML Document for EML File**\n");
        prompt.append("Create a complete, well-formatted HTML document suitable for saving as an EML file.\n");
        prompt.append("Use proper HTML structure with comprehensive styling for optimal email client viewing.\n\n");

        prompt.append("**Required HTML Structure:**\n");
        prompt.append("```html\n");
        prompt.append("<!DOCTYPE html>\n");
        prompt.append("<html lang=\"en\">\n");
        prompt.append("<head>\n");
        prompt.append("    <meta charset=\"UTF-8\">\n");
        prompt.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        prompt.append("    <title>Day ").append(learningDay.getDay()).append(" - Programming Challenges</title>\n");
        prompt.append("    <style>\n");
        prompt.append("        /* Include comprehensive CSS for email client compatibility */\n");
        prompt.append("        body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; background: #f5f7fa; }\n");
        prompt.append("        .container { max-width: 800px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); overflow: hidden; }\n");
        prompt.append("        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }\n");
        prompt.append("        .problem-section { padding: 25px; border-bottom: 2px solid #f1f3f4; }\n");
        prompt.append("        .difficulty-easy { border-left: 5px solid #4CAF50; }\n");
        prompt.append("        .difficulty-medium { border-left: 5px solid #FF9800; }\n");
        prompt.append("        .difficulty-hard { border-left: 5px solid #F44336; }\n");
        prompt.append("        .problem-title { font-size: 1.4em; font-weight: bold; margin-bottom: 15px; }\n");
        prompt.append("        .problem-description { margin-bottom: 20px; line-height: 1.7; }\n");
        prompt.append("        .code-block { background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; padding: 15px; font-family: 'Courier New', monospace; overflow-x: auto; }\n");
        prompt.append("        .hints-section { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 15px 0; }\n");
        prompt.append("        .solution-approach { background: #d1ecf1; border-left: 4px solid #17a2b8; padding: 15px; margin: 15px 0; }\n");
        prompt.append("    </style>\n");
        prompt.append("</head>\n");
        prompt.append("<body>\n");
        prompt.append("    <!-- Header with day info -->\n");
        prompt.append("    <!-- Problem 1: Easy -->\n");
        prompt.append("    <!-- Problem 2: Medium -->\n");
        prompt.append("    <!-- Problem 3: Hard -->\n");
        prompt.append("    <!-- Footer with submission guidelines -->\n");
        prompt.append("</body>\n");
        prompt.append("</html>\n");
        prompt.append("```\n\n");

        prompt.append("**Content Requirements for Each Problem:**\n");
        prompt.append("1. **Problem Title** - Clear, descriptive name\n");
        prompt.append("2. **Difficulty Badge** - Visual indicator (Easy/Medium/Hard)\n");
        prompt.append("3. **Problem Statement** - Clear description of what to solve\n");
        prompt.append("4. **Input/Output Format** - Specify expected inputs and outputs\n");
        prompt.append("5. **Example Test Cases** - 2-3 examples with explanations\n");
        prompt.append("6. **Constraints** - Technical limitations and requirements\n");
        prompt.append("7. **Hints Section** - Subtle guidance without giving away the solution\n");
        prompt.append("8. **Solution Approach** - High-level strategy (not full code)\n");
        prompt.append("9. **Time/Space Complexity** - Expected efficiency requirements\n\n");

        prompt.append("**Problem Progression Guidelines:**\n");
        prompt.append("- **EASY**: Focus on ").append(learningDay.getAlgorithmTask()).append(" with direct application\n");
        prompt.append("- **MEDIUM**: Combine ").append(learningDay.getAlgorithmTask()).append(" with ").append(learningDay.getTheoryTask()).append("\n");
        prompt.append("- **HARD**: Advanced scenario requiring ").append(learningDay.getCodingTask()).append(" and optimization\n\n");

        prompt.append("**Specific Requirements:**\n");
        prompt.append("- Problems must be practical and relate to real-world scenarios\n");
        prompt.append("- Include multiple programming languages where applicable (Java, Python, JavaScript)\n");
        prompt.append("- Provide clear code examples and pseudo-code\n");
        prompt.append("- Make content engaging and motivational\n");
        prompt.append("- Focus on progressive skill building appropriate for ").append(learningDay.getPhase()).append(" phase\n");
        prompt.append("- Include estimated time to solve each problem\n");
        prompt.append("- Add reflection questions for learning consolidation\n\n");

        prompt.append("**Output the complete HTML document only (no additional text before or after).**\n");

        log.info("📝 Generated problem prompt for Day {} ({} characters)", learningDay.getDay(), prompt.length());
        return prompt.toString();
    }

    /**
     * Get AI response for problem generation
     */
    private String getAIResponse(String prompt) {
        log.info("🤖 Requesting AI response for problem generation...");

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

            // Add generation config for problem generation
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.8); // Slightly higher for creative problems
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 50000);
            requestBody.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("🌐 Sending problem generation request to Gemini API...");
            ResponseEntity<Map> response = restTemplate.postForEntity(geminiApiUrl, request, Map.class);

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Object candidates = responseBody.get("candidates");
                
                if (candidates instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> candidatesList = (java.util.List<Map<String, Object>>) candidates;
                    
                    if (!candidatesList.isEmpty()) {
                        Map<String, Object> firstCandidate = candidatesList.get(0);
                        Object responseContent = firstCandidate.get("content");
                        
                        if (responseContent instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> contentMap = (Map<String, Object>) responseContent;
                            Object parts = contentMap.get("parts");
                            
                            if (parts instanceof java.util.List) {
                                @SuppressWarnings("unchecked")
                                java.util.List<Map<String, Object>> partsList = (java.util.List<Map<String, Object>>) parts;
                                
                                if (!partsList.isEmpty()) {
                                    Object text = partsList.get(0).get("text");
                                    if (text instanceof String) {
                                        String result = ((String) text).trim();
                                        log.info("✅ AI problem content generated successfully ({} characters)", result.length());
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            log.warn("⚠️ Empty or invalid AI response for problem generation");
            return null;

        } catch (Exception e) {
            log.error("❌ Error calling Gemini API for problems: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate fallback problem content when AI is unavailable
     */
    private String generateFallbackProblemContent(LearningDay learningDay) {
        log.warn("🔄 Generating fallback problem content for Day {}", learningDay.getDay());

        StringBuilder fallback = new StringBuilder();
        
        fallback.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>Day ").append(learningDay.getDay()).append(" - Programming Challenges</title>\n")
            .append("    <style>\n")
            .append("        body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; background: #f5f7fa; }\n")
            .append("        .container { max-width: 800px; margin: 0 auto; background: white; border-radius: 12px; padding: 30px; }\n")
            .append("        .header { text-align: center; margin-bottom: 30px; }\n")
            .append("        .problem-section { margin-bottom: 40px; padding: 20px; border-left: 5px solid #007bff; }\n")
            .append("        .problem-title { font-size: 1.4em; font-weight: bold; margin-bottom: 15px; }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <div class=\"container\">\n")
            .append("        <div class=\"header\">\n")
            .append("            <h1>Day ").append(learningDay.getDay()).append(" - Programming Challenges</h1>\n")
            .append("            <p>Phase: ").append(learningDay.getPhase()).append("</p>\n")
            .append("            <p><em>AI service temporarily unavailable - Basic problems provided</em></p>\n")
            .append("        </div>\n");

        // Add basic problems
        fallback.append("        <div class=\"problem-section\">\n")
            .append("            <div class=\"problem-title\">🟢 EASY: Basic Algorithm Practice</div>\n")
            .append("            <p><strong>Task:</strong> ").append(learningDay.getAlgorithmTask()).append("</p>\n")
            .append("            <p><strong>Focus:</strong> Implement the basic version and test with simple examples.</p>\n")
            .append("            <p><strong>Time:</strong> 30-45 minutes</p>\n")
            .append("        </div>\n");

        fallback.append("        <div class=\"problem-section\">\n")
            .append("            <div class=\"problem-title\">🟡 MEDIUM: Applied Theory</div>\n")
            .append("            <p><strong>Task:</strong> ").append(learningDay.getTheoryTask()).append("</p>\n")
            .append("            <p><strong>Focus:</strong> Apply theoretical concepts in a practical coding scenario.</p>\n")
            .append("            <p><strong>Time:</strong> 45-60 minutes</p>\n")
            .append("        </div>\n");

        fallback.append("        <div class=\"problem-section\">\n")
            .append("            <div class=\"problem-title\">🔴 HARD: Advanced Challenge</div>\n")
            .append("            <p><strong>Task:</strong> ").append(learningDay.getCodingTask()).append("</p>\n")
            .append("            <p><strong>Focus:</strong> Combine multiple concepts and optimize for efficiency.</p>\n")
            .append("            <p><strong>Time:</strong> 60-90 minutes</p>\n")
            .append("        </div>\n");

        fallback.append("        <div style=\"background: #fff3cd; padding: 15px; border-radius: 6px;\">\n")
            .append("            <h3>📝 Reflection Questions</h3>\n")
            .append("            <p>After solving the problems, consider:</p>\n")
            .append("            <ul>\n")
            .append("                <li>Which problem was most challenging and why?</li>\n")
            .append("                <li>How did you approach each difficulty level?</li>\n")
            .append("                <li>What concepts from ").append(learningDay.getPhase()).append(" did you apply?</li>\n")
            .append("                <li>What would you do differently next time?</li>\n")
            .append("            </ul>\n")
            .append("        </div>\n")
            .append("    </div>\n")
            .append("</body>\n")
            .append("</html>");

        return fallback.toString();
    }
}
