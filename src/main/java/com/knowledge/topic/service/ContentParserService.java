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
            log.debug("Parsing overview content: {}", jsonContent.substring(0, Math.min(jsonContent.length(), 200)));
            
            // Clean and extract JSON content
            String cleanJson = extractJsonFromResponse(jsonContent);
            
            if (cleanJson != null && !cleanJson.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(cleanJson);
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
            log.debug("Parsing detailed content: {} chars", jsonContent.length());
            
            // Check if content appears to be truncated
            if (jsonContent.length() > 5000 && !jsonContent.trim().endsWith("}")) {
                log.warn("Content appears to be truncated ({}+ chars, doesn't end with })", jsonContent.length());
            }
            
            // Clean and extract JSON content
            String cleanJson = extractJsonFromResponse(jsonContent);
            
            if (cleanJson != null && !cleanJson.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(cleanJson);
                
                // Build field names list for logging
                StringBuilder fieldNames = new StringBuilder();
                jsonNode.fieldNames().forEachRemaining(name -> {
                    if (fieldNames.length() > 0) fieldNames.append(", ");
                    fieldNames.append(name);
                });
                log.info("Successfully parsed JSON. Root keys: {}", 
                    fieldNames.length() > 0 ? fieldNames.toString() : "no fields");
                
                // Parse the new structure with error handling
                ParsedDetail result = ParsedDetail.builder()
                        .executiveSummary(parseOverviewSection(jsonNode.get("overview")))
                        .historicalEvolution(parseKeyConceptsSection(jsonNode.get("keyConcepts")))
                        .corePrinciples(parseCodeExamples(jsonNode.get("codeExamples")))
                        .realWorldApplications(parseCodeExamplesAsApplications(jsonNode.get("codeExamples")))
                        .caseStudies(parseAdvancedTechniques(jsonNode.get("advancedTechniques")))
                        .interconnectedConcepts(parseRelatedTopicsSection(jsonNode.get("relatedTopics")))
                        .currentInnovation(parseBestPracticesSection(jsonNode.get("bestPractices")))
                        .futureOutlook(parseComparisonsSection(jsonNode.get("comparisons")))
                        .expertInsights(parseBestPracticesAsInsights(jsonNode.get("bestPractices")))
                        .learningResources(parseLearningResources(jsonNode.get("learningResources")))
                        .keyMetrics(getArrayValues(jsonNode, "keyMetrics"))
                        .build();
                
                log.info("Successfully parsed detailed content with {} sections", 
                    (result.getExecutiveSummary().isEmpty() ? 0 : 1) +
                    (result.getCorePrinciples().isEmpty() ? 0 : 1) +
                    result.getRealWorldApplications().size() +
                    result.getCaseStudies().size());
                        
                return result;
            } else {
                log.warn("Failed to extract JSON from response. Content preview: {}", 
                    jsonContent.substring(0, Math.min(jsonContent.length(), 300)));
                // Fallback: treat as plain text
                return parseDetailFromText(jsonContent);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse detailed JSON content. Error: {} | Content preview: {}", 
                e.getMessage(), 
                jsonContent.substring(0, Math.min(jsonContent.length(), 300)));
            return parseDetailFromText(jsonContent);
        } catch (Exception e) {
            log.error("Unexpected error parsing detailed content: {}", e.getMessage(), e);
            return parseDetailFromText(jsonContent);
        }
    }

    /**
     * Extract JSON content from AI response, handling various formats and cleaning
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return null;
        }

        log.debug("Extracting JSON from response. Content length: {}", response.length());
        
        // Remove markdown code fences if present - improved regex
        String cleaned = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        
        // Also handle cases where JSON might be in plain text after certain markers
        if (cleaned.contains("\"overview\"") && cleaned.contains("\"keyConcepts\"")) {
            // This looks like our JSON structure, let's extract it more carefully
            int jsonStart = cleaned.indexOf("{");
            if (jsonStart >= 0) {
                // Find the matching closing brace by counting braces, handling strings properly
                int braceCount = 0;
                int jsonEnd = -1;
                boolean inString = false;
                boolean escaped = false;
                
                for (int i = jsonStart; i < cleaned.length(); i++) {
                    char c = cleaned.charAt(i);
                    
                    // Handle escape sequences
                    if (escaped) {
                        escaped = false;
                        continue;
                    }
                    
                    if (c == '\\') {
                        escaped = true;
                        continue;
                    }
                    
                    // Handle string boundaries
                    if (c == '"') {
                        inString = !inString;
                        continue;
                    }
                    
                    // Only count braces outside of strings
                    if (!inString) {
                        if (c == '{') {
                            braceCount++;
                        } else if (c == '}') {
                            braceCount--;
                            if (braceCount == 0) {
                                jsonEnd = i;
                                break;
                            }
                        }
                    }
                }
                
                if (jsonEnd > jsonStart) {
                    String extracted = cleaned.substring(jsonStart, jsonEnd + 1);
                    
                    // Clean up any HTML entities or malformed content
                    extracted = extracted
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&amp;", "&")
                        .replace("&quot;", "\"")
                        .trim();
                        
                    // Validate JSON before returning
                    try {
                        objectMapper.readTree(extracted);
                        log.debug("Successfully extracted and validated JSON: {} chars", extracted.length());
                        return extracted;
                    } catch (JsonProcessingException e) {
                        log.warn("Extracted JSON is malformed: {}", e.getMessage());
                        // Try to repair the JSON
                        return attemptJsonRepair(extracted);
                    }
                } else {
                    log.warn("Could not find matching closing brace for JSON");
                    // Try to repair incomplete JSON
                    String partial = cleaned.substring(jsonStart);
                    return attemptJsonRepair(partial);
                }
            }
        }
        
        // Fallback to original method
        int jsonStart = cleaned.indexOf("{");
        int jsonEnd = cleaned.lastIndexOf("}");
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String extracted = cleaned.substring(jsonStart, jsonEnd + 1);
            
            // Clean up any HTML entities or malformed content
            extracted = extracted
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .trim();
                
            try {
                objectMapper.readTree(extracted);
                log.debug("Fallback JSON extraction successful: {} chars", extracted.length());
                return extracted;
            } catch (JsonProcessingException e) {
                log.warn("Fallback JSON is malformed: {}", e.getMessage());
                return attemptJsonRepair(extracted);
            }
        }
        
        log.warn("No valid JSON found in response");
        return null;
    }
    
    /**
     * Attempt to repair malformed JSON by fixing common issues
     */
    private String attemptJsonRepair(String brokenJson) {
        if (brokenJson == null || brokenJson.isEmpty()) {
            return null;
        }
        
        try {
            log.debug("Attempting to repair JSON: {} chars", brokenJson.length());
            
            String repaired = brokenJson.trim();
            
            // Handle case where JSON is truncated in the middle
            if (!repaired.endsWith("}")) {
                repaired = repairTruncatedJson(repaired);
            }
            
            // Try to parse the repaired JSON
            objectMapper.readTree(repaired);
            log.info("Successfully repaired malformed JSON: {} -> {} chars", brokenJson.length(), repaired.length());
            return repaired;
            
        } catch (JsonProcessingException e) {
            log.warn("Failed to repair JSON: {}", e.getMessage());
            // As last resort, try to extract just the overview section which is usually complete
            return extractPartialContent(brokenJson);
        }
    }
    
    /**
     * Repair JSON that has been truncated in the middle of content
     */
    private String repairTruncatedJson(String json) {
        StringBuilder repaired = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        int braceLevel = 0;
        int arrayLevel = 0;
        int lastGoodPosition = 0;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escaped) {
                escaped = false;
                repaired.append(c);
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                repaired.append(c);
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                repaired.append(c);
                if (!inString) {
                    // Exiting a string - this might be a good stopping point
                    lastGoodPosition = repaired.length();
                }
                continue;
            }
            
            if (!inString) {
                if (c == '{') {
                    braceLevel++;
                    repaired.append(c);
                } else if (c == '}') {
                    braceLevel--;
                    repaired.append(c);
                    if (braceLevel >= 0) {
                        lastGoodPosition = repaired.length();
                    }
                } else if (c == '[') {
                    arrayLevel++;
                    repaired.append(c);
                } else if (c == ']') {
                    arrayLevel--;
                    repaired.append(c);
                    if (arrayLevel >= 0) {
                        lastGoodPosition = repaired.length();
                    }
                } else if (c == ',' || c == ':' || Character.isWhitespace(c)) {
                    repaired.append(c);
                    if (c == ',') {
                        lastGoodPosition = repaired.length();
                    }
                } else {
                    repaired.append(c);
                }
            } else {
                repaired.append(c);
            }
        }
        
        // If we're in the middle of a string or have unclosed structures, truncate to last good position
        if (inString || braceLevel > 0 || arrayLevel > 0) {
            if (lastGoodPosition > 0) {
                repaired = new StringBuilder(repaired.substring(0, lastGoodPosition));
                
                // Remove trailing comma if present
                String result = repaired.toString().trim();
                if (result.endsWith(",")) {
                    result = result.substring(0, result.length() - 1);
                }
                
                // Close any open arrays
                while (arrayLevel > 0) {
                    result += "]";
                    arrayLevel--;
                }
                
                // Close any open objects
                while (braceLevel > 0) {
                    result += "}";
                    braceLevel--;
                }
                
                return result;
            }
        }
        
        return repaired.toString();
    }
    
    /**
     * Extract partial content when JSON repair fails - focus on overview section
     */
    private String extractPartialContent(String brokenJson) {
        try {
            // Try to extract just the overview section which is usually at the beginning
            int overviewStart = brokenJson.indexOf("\"overview\"");
            if (overviewStart > 0) {
                int definitionStart = brokenJson.indexOf("\"definition\"", overviewStart);
                if (definitionStart > 0) {
                    int definitionEnd = brokenJson.indexOf("\",", definitionStart);
                    if (definitionEnd > 0) {
                        String definition = brokenJson.substring(definitionStart + 13, definitionEnd);
                        // Create a minimal valid JSON with just the overview
                        return String.format("""
                            {
                              "overview": {
                                "definition": "%s",
                                "importance": "This topic is important for professional development",
                                "historicalContext": "This topic has evolved over time with technological advances"
                              },
                              "keyConcepts": [],
                              "codeExamples": [],
                              "bestPractices": { "dos": [], "donts": [], "optimizationTips": [] },
                              "comparisons": { "alternatives": [], "summary": "" },
                              "advancedTechniques": { "specializedScenarios": [], "caseStudies": [] },
                              "relatedTopics": [],
                              "learningResources": [],
                              "keyMetrics": []
                            }
                            """, definition.replace("\"", "\\\""));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract partial content: {}", e.getMessage());
        }
        
        return null;
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

    private List<ParsedDetail.ExpertInsight> parseBestPracticesAsInsights(JsonNode bestPracticesNode) {
        List<ParsedDetail.ExpertInsight> insights = new ArrayList<>();
        
        if (bestPracticesNode != null) {
            JsonNode dos = bestPracticesNode.get("dos");
            if (dos != null && dos.isArray()) {
                for (int i = 0; i < Math.min(dos.size(), 2); i++) {
                    insights.add(ParsedDetail.ExpertInsight.builder()
                            .expert("Best Practices Expert")
                            .insight("✅ " + dos.get(i).asText())
                            .build());
                }
            }
            
            JsonNode donts = bestPracticesNode.get("donts");
            if (donts != null && donts.isArray()) {
                for (int i = 0; i < Math.min(donts.size(), 2); i++) {
                    insights.add(ParsedDetail.ExpertInsight.builder()
                            .expert("Best Practices Expert")
                            .insight("❌ " + donts.get(i).asText())
                            .build());
                }
            }
        }
        
        return insights;
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

    // New parsing methods for updated JSON structure
    
    private String parseOverviewSection(JsonNode overviewNode) {
        if (overviewNode == null) return "";
        
        StringBuilder overview = new StringBuilder();
        overview.append("<h3>📋 Definition</h3>");
        overview.append("<p>").append(getTextValue(overviewNode, "definition")).append("</p>");
        
        overview.append("<h3>⭐ Importance</h3>");
        overview.append("<p>").append(getTextValue(overviewNode, "importance")).append("</p>");
        
        overview.append("<h3>🕰️ Historical Context</h3>");
        overview.append("<p>").append(getTextValue(overviewNode, "historicalContext")).append("</p>");
        
        return overview.toString();
    }
    
    private String parseKeyConceptsSection(JsonNode conceptsNode) {
        if (conceptsNode == null || !conceptsNode.isArray()) return "";
        
        StringBuilder concepts = new StringBuilder();
        concepts.append("<h3>💡 Core Concepts</h3>");
        
        conceptsNode.forEach(concept -> {
            concepts.append("<div class='concept-item'>");
            concepts.append("<h4>").append(getTextValue(concept, "title")).append("</h4>");
            concepts.append("<p>").append(getTextValue(concept, "description")).append("</p>");
            concepts.append("<div class='example'><strong>Example:</strong> ");
            concepts.append(getTextValue(concept, "example")).append("</div>");
            concepts.append("</div>");
        });
        
        return concepts.toString();
    }
    
    private List<ParsedDetail.Application> parseCodeExamplesAsApplications(JsonNode codeExamplesNode) {
        List<ParsedDetail.Application> applications = new ArrayList<>();
        
        if (codeExamplesNode == null || !codeExamplesNode.isArray()) {
            // Return default applications if no code examples
            applications.add(ParsedDetail.Application.builder()
                    .title("Industry Applications")
                    .description("Widely used across various industries for solving specific problems and implementing efficient solutions.")
                    .impact("Significant business value and efficiency gains")
                    .build());
            return applications;
        }
        
        for (JsonNode example : codeExamplesNode) {
            String title = getNodeText(example, "title");
            String explanation = getNodeText(example, "explanation");
            String language = getNodeText(example, "language");
            
            if (!title.isEmpty()) {
                applications.add(ParsedDetail.Application.builder()
                        .title(title + " Application")
                        .description(explanation)
                        .impact("Practical implementation using " + language.toUpperCase() + " programming")
                        .build());
            }
        }
        
        return applications;
    }

    private String parseCodeExamples(JsonNode codeExamplesNode) {
        if (codeExamplesNode == null || !codeExamplesNode.isArray()) {
            return "Key principles include understanding core concepts, implementing best practices, and maintaining clean, efficient code.";
        }
        
        StringBuilder examples = new StringBuilder();
        examples.append("<h4>Core Programming Principles</h4>");
        
        for (JsonNode example : codeExamplesNode) {
            String title = getNodeText(example, "title");
            String language = getNodeText(example, "language");
            String code = getNodeText(example, "code");
            String explanation = getNodeText(example, "explanation");
            
            if (!title.isEmpty()) {
                examples.append("<div class='code-example'>");
                examples.append("<h5>").append(title).append("</h5>");
                
                if (!code.isEmpty()) {
                    examples.append("<div class='code-block'>");
                    examples.append("<span class='language-tag'>").append(language.toUpperCase()).append("</span>");
                    examples.append("<pre><code class='language-").append(language).append("'>");
                    examples.append(escapeHtml(code.replace("\\n", "\n")));
                    examples.append("</code></pre>");
                    examples.append("</div>");
                }
                
                if (!explanation.isEmpty()) {
                    examples.append("<p class='explanation'>").append(explanation).append("</p>");
                }
                
                examples.append("</div>");
            }
        }
        
        return examples.toString();
    }
    
    private List<ParsedDetail.CaseStudy> parseAdvancedTechniques(JsonNode advancedNode) {
        List<ParsedDetail.CaseStudy> techniques = new ArrayList<>();
        if (advancedNode != null) {
            JsonNode caseStudies = advancedNode.get("caseStudies");
            if (caseStudies != null && caseStudies.isArray()) {
                caseStudies.forEach(study -> {
                    techniques.add(ParsedDetail.CaseStudy.builder()
                            .company(getTextValue(study, "company"))
                            .challenge(getTextValue(study, "challenge"))
                            .solution(getTextValue(study, "solution"))
                            .results(getTextValue(study, "results"))
                            .build());
                });
            }
            
            // Also parse specialized scenarios
            JsonNode scenarios = advancedNode.get("specializedScenarios");
            if (scenarios != null && scenarios.isArray()) {
                scenarios.forEach(scenario -> {
                    techniques.add(ParsedDetail.CaseStudy.builder()
                            .company(getTextValue(scenario, "scenario"))
                            .challenge("Advanced Scenario")
                            .solution(getTextValue(scenario, "techniques"))
                            .results(getTextValue(scenario, "realWorldExample"))
                            .build());
                });
            }
        }
        return techniques;
    }
    
    private String parseRelatedTopicsSection(JsonNode relatedNode) {
        if (relatedNode == null || !relatedNode.isArray()) return "";
        
        StringBuilder related = new StringBuilder();
        related.append("<h3>🔗 Related Topics for Further Learning</h3>");
        related.append("<ul>");
        
        relatedNode.forEach(topic -> {
            related.append("<li>");
            related.append("<strong>").append(getTextValue(topic, "topic")).append("</strong> - ");
            related.append(getTextValue(topic, "connection"));
            related.append(" <em>(").append(getTextValue(topic, "learningOrder")).append(")</em>");
            related.append("</li>");
        });
        
        related.append("</ul>");
        return related.toString();
    }
    
    private String parseBestPracticesSection(JsonNode practicesNode) {
        if (practicesNode == null) return "";
        
        StringBuilder practices = new StringBuilder();
        practices.append("<h3>✅ Best Practices & Guidelines</h3>");
        
        // Parse DOS
        JsonNode dos = practicesNode.get("dos");
        if (dos != null && dos.isArray()) {
            practices.append("<h4>👍 DO:</h4><ul>");
            dos.forEach(doItem -> {
                practices.append("<li>").append(doItem.asText()).append("</li>");
            });
            practices.append("</ul>");
        }
        
        // Parse DON'TS
        JsonNode donts = practicesNode.get("donts");
        if (donts != null && donts.isArray()) {
            practices.append("<h4>👎 DON'T:</h4><ul>");
            donts.forEach(dontItem -> {
                practices.append("<li>").append(dontItem.asText()).append("</li>");
            });
            practices.append("</ul>");
        }
        
        // Parse optimization tips
        JsonNode tips = practicesNode.get("optimizationTips");
        if (tips != null && tips.isArray()) {
            practices.append("<h4>🚀 Optimization Tips:</h4><ul>");
            tips.forEach(tip -> {
                practices.append("<li>").append(tip.asText()).append("</li>");
            });
            practices.append("</ul>");
        }
        
        return practices.toString();
    }
    
    private String parseComparisonsSection(JsonNode comparisonsNode) {
        if (comparisonsNode == null) return "";
        
        StringBuilder comparisons = new StringBuilder();
        comparisons.append("<h3>⚖️ Comparisons & Alternatives</h3>");
        
        JsonNode alternatives = comparisonsNode.get("alternatives");
        if (alternatives != null && alternatives.isArray()) {
            alternatives.forEach(alt -> {
                comparisons.append("<div class='comparison-item'>");
                comparisons.append("<h4>").append(getTextValue(alt, "name")).append("</h4>");
                comparisons.append("<p><strong>Differences:</strong> ").append(getTextValue(alt, "differences")).append("</p>");
                comparisons.append("<p><strong>Advantages:</strong> ").append(getTextValue(alt, "advantages")).append("</p>");
                comparisons.append("<p><strong>When to use alternative:</strong> ").append(getTextValue(alt, "disadvantages")).append("</p>");
                comparisons.append("</div>");
            });
        }
        
        String summary = getTextValue(comparisonsNode, "summary");
        if (!summary.isEmpty()) {
            comparisons.append("<p><strong>Summary:</strong> ").append(summary).append("</p>");
        }
        
        return comparisons.toString();
    }
    
    private String formatCodeExample(String code, String language) {
        if (code == null || code.trim().isEmpty()) return "";
        
        // Format code with proper syntax highlighting placeholders
        return String.format("<pre class='language-%s'><code>%s</code></pre>", 
                           language != null ? language : "java", 
                           code.replace("\\n", "\n")
                                .replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;"));
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
    
    // Helper methods
    private String getNodeText(JsonNode node, String fieldName) {
        if (node == null || node.get(fieldName) == null) {
            return "";
        }
        return node.get(fieldName).asText();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
