package com.knowledge.stock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import com.knowledge.stock.model.StockLearningDay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockEmailService {

    private final JavaMailSender mailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${app.mail-from}")
    private String fromEmail;

    @Value("${app.mail-to}")
    private String toEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Send structured learning email using HTML template
     */
    public void sendStructuredLearningEmail(StockLearningDay learningDay, String content) {
        log.info("📧 Preparing structured learning email for Day {}: {}",
            learningDay.getDay(), learningDay.getTopic());

        if (!emailEnabled) {
            log.warn("📨 Email sending is disabled. Would have sent Day {} email", learningDay.getDay());
            return;
        }

        try {
            String subject = learningDay.getEmailSubject();
            String htmlContent = loadAndProcessTemplate(learningDay, content);

            log.debug("📝 Email prepared - Subject: {}", subject);
            log.debug("📄 Email content length: {} characters", htmlContent.length());

            sendEmail(subject, htmlContent);

            log.info("✅ Structured learning email sent successfully for Day {} - {}",
                learningDay.getDay(), learningDay.getTopic());

        } catch (Exception e) {
            log.error("❌ Failed to send structured learning email for Day {}: {}",
                learningDay.getDay(), e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    /**
     * Load HTML template and replace placeholders
     */
    private String loadAndProcessTemplate(StockLearningDay learningDay, String content) throws IOException {
        log.debug("🎨 Loading and processing HTML template for Day {}", learningDay.getDay());

        // Load the HTML template
        ClassPathResource resource = new ClassPathResource("stock-knowledge-email-template.html");
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // Replace placeholders with actual data
        String processedHtml = template
            .replace("{{DAY}}", String.valueOf(learningDay.getDay()))
            .replace("{{WEEK}}", learningDay.getWeek())
            .replace("{{PHASE}}", learningDay.getPhase())
            .replace("{{TOPIC}}", learningDay.getTopic())
            .replace("{{LEARNING_GOAL}}", learningDay.getLearningGoal())
            .replace("{{CONTENT}}", parseJsonToStructuredHtml(content))
            .replace("{{PRACTICE_TASK}}", learningDay.getPracticeTask())
            .replace("{{TIMESTAMP}}", LocalDateTime.now().format(DATE_FORMATTER));

        log.debug("✅ Template processed successfully ({} characters)", processedHtml.length());
        return processedHtml;
    }

    /**
     * Send actual email
     */
    private void sendEmail(String subject, String htmlContent) throws MessagingException {
        log.debug("📮 Sending email with subject: {}", subject);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail.split(","));
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.debug("📬 Email sent successfully to: {}", toEmail);
    }

    /**
     * Parse JSON content and convert to structured HTML with proper containers
     */
    private String parseJsonToStructuredHtml(String jsonContent) {
        log.debug("🔧 Parsing JSON content to structured HTML with enhanced formatting");

        try {
            // Clean up the JSON content (remove any markdown formatting around it)
            String cleanJson = cleanJsonContent(jsonContent);

            JsonNode rootNode = objectMapper.readTree(cleanJson);
            StringBuilder htmlBuilder = new StringBuilder();

            // Add content sections grid container
            htmlBuilder.append("<div class=\"content-sections-grid\">\n");

            // Process each section with enhanced formatting and better separation
            processSection(htmlBuilder, rootNode, "introduction", "introduction-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "coreConceptsDefinitions", "core-concepts-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "examples", "examples-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "stepByStepGuide", "next-steps-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "commonMistakes", "common-mistakes-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "keyTakeaways", "key-takeaways-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "nextSteps", "additional-resources-container");

            // Close content sections grid
            htmlBuilder.append("</div>\n");

            String result = htmlBuilder.toString();
            log.debug("✅ JSON content parsed successfully with enhanced formatting ({} characters)", result.length());
            return result;

        } catch (Exception e) {
            log.error("❌ Failed to parse JSON content: {}", e.getMessage());
            // Fallback to basic markdown formatting if JSON parsing fails
            return formatMarkdownToHtml(jsonContent);
        }
    }

    /**
     * Add visual content separator between sections
     */
    private void addContentSeparator(StringBuilder htmlBuilder) {
        htmlBuilder.append("<hr class=\"content-separator\">\n");
    }

    /**
     * Process individual section and add to HTML with proper container structure
     */
    private void processSection(StringBuilder htmlBuilder, JsonNode rootNode, String sectionKey, String containerClass) {
        JsonNode sectionNode = rootNode.get(sectionKey);
        if (sectionNode != null && !sectionNode.isNull()) {
            htmlBuilder.append("<div class=\"json-content-section ").append(containerClass).append("\">\n");

            // Add section header with proper styling
            htmlBuilder.append("  <div class=\"section-header\">\n");
            JsonNode titleNode = sectionNode.get("title");
            if (titleNode != null && !titleNode.isNull()) {
                htmlBuilder.append("    <h2>").append(escapeHtml(titleNode.asText())).append("</h2>\n");
            }
            htmlBuilder.append("  </div>\n");

            // Add section content with proper container
            htmlBuilder.append("  <div class=\"section-content\">\n");
            JsonNode contentNode = sectionNode.get("content");
            if (contentNode != null && !contentNode.isNull()) {
                String content = contentNode.asText();
                htmlBuilder.append("    ").append(formatEnhancedContent(content, sectionKey)).append("\n");
            } else {
                htmlBuilder.append("    <p><em>Content not available for this section.</em></p>\n");
            }
            htmlBuilder.append("  </div>\n");

            htmlBuilder.append("</div>\n\n");
        }
    }

    /**
     * Enhanced content formatting with section-specific styling
     */
    private String formatEnhancedContent(String content, String sectionType) {
        if (content == null || content.trim().isEmpty()) {
            return "<p><em>No content available.</em></p>";
        }

        // Clean up the content first
        String cleanContent = content.trim();

        // If content already contains HTML tags, clean it up and enhance it
        if (cleanContent.contains("<") && cleanContent.contains(">")) {
            return enhanceExistingHtml(cleanContent, sectionType);
        }

        // Convert markdown-style formatting to enhanced HTML
        String htmlContent = cleanContent
            .replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>")  // **bold** to <strong>
            .replaceAll("\\*([^*]+)\\*", "<em>$1</em>")                // *italic* to <em>
            .replaceAll("(?m)^#{3}\\s+(.+)$", "<h4>$1</h4>")          // ### to h4
            .replaceAll("(?m)^#{2}\\s+(.+)$", "<h3>$1</h3>")          // ## to h3
            .replaceAll("(?m)^- (.+)$", "<li>$1</li>")                 // - item to <li>
            .replaceAll("(?m)^\\d+\\. (.+)$", "<li>$1</li>")           // 1. item to <li>
            .replaceAll("\\n\\n", "</p>\n<p>")                         // Double newlines to paragraphs
            .replaceAll("\\n", "<br>\n");                              // Single newlines to breaks

        // Wrap in paragraphs if not already formatted
        if (!htmlContent.startsWith("<")) {
            htmlContent = "<p>" + htmlContent + "</p>";
        }

        // Wrap consecutive <li> items in proper lists
        htmlContent = wrapListItems(htmlContent);

        // Add section-specific enhancements
        htmlContent = addSectionSpecificFormatting(htmlContent, sectionType);

        return htmlContent;
    }

    /**
     * Enhance existing HTML content with section-specific formatting
     */
    private String enhanceExistingHtml(String content, String sectionType) {
        String enhanced = content
            .replaceAll("<p></p>", "")
            .replaceAll("<p>\\s*<h([1-6])>", "<h$1>")
            .replaceAll("</h([1-6])>\\s*</p>", "</h$1>")
            .trim();

        return addSectionSpecificFormatting(enhanced, sectionType);
    }

    /**
     * Wrap consecutive list items in proper ul/ol tags
     */
    private String wrapListItems(String content) {
        // Handle unordered lists
        String result = content.replaceAll("(<li>.*?</li>)", "<ul>$1</ul>");
        result = result.replaceAll("</ul>\\s*<ul>", ""); // Merge consecutive ul tags

        return result;
    }

    /**
     * Add section-specific formatting and styling
     */
    private String addSectionSpecificFormatting(String content, String sectionType) {
        switch (sectionType) {
            case "coreConceptsDefinitions":
                // Wrap definitions in special blocks
                content = content.replaceAll("<strong>([^<]+)</strong>:",
                    "<div class=\"definition-block\"><strong>$1</strong>:");
                break;

            case "examples":
                // Wrap examples in special blocks
                content = content.replaceAll("<strong>([^<]+):</strong>",
                    "<div class=\"example-block\"><strong>$1:</strong>");
                break;

            case "commonMistakes":
                // Wrap warnings in special blocks
                content = content.replaceAll("<li>([^<]+)</li>",
                    "<div class=\"warning-block\"><li>$1</li></div>");
                break;

            case "keyTakeaways":
                // Enhance key takeaways with special formatting
                content = content.replaceAll("<li>([^<]+)</li>",
                    "<div class=\"tip-block\"><li>$1</li></div>");
                break;
        }

        return content;
    }

    /**
     * Clean JSON content by removing any surrounding markdown or extra text
     */
    private String cleanJsonContent(String content) {
        // Remove markdown code blocks if present
        String cleaned = content.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "");

        // Find the JSON object start and end
        int jsonStart = cleaned.indexOf('{');
        int jsonEnd = cleaned.lastIndexOf('}');

        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
        }

        return cleaned.trim();
    }

    /**
     * Escape HTML characters for safe display
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /**
     * Fallback method for non-JSON content using markdown-style formatting
     */
    private String formatMarkdownToHtml(String content) {
        log.debug("🔄 Using fallback markdown formatting");

        // Enhanced markdown-style formatting to HTML
        String htmlContent = content
            .replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>")  // **bold** to <strong>
            .replaceAll("\\*([^*]+)\\*", "<em>$1</em>")                // *italic* to <em>
            .replaceAll("(?m)^#{3}\\s+(.+)$", "<h3>$1</h3>")          // ### heading to h3
            .replaceAll("(?m)^#{2}\\s+(.+)$", "<h2>$1</h2>")          // ## heading to h2
            .replaceAll("(?m)^#{1}\\s+(.+)$", "<h1>$1</h1>")          // # heading to h1
            .replaceAll("\\n\\n", "</p><p>")                           // Double newlines to paragraphs
            .replaceAll("\\n", "<br>")                                 // Single newlines to breaks
            .replaceAll("^(.)", "<p>$1")                               // Start with paragraph
            .replaceAll("(.)$", "$1</p>");                             // End with paragraph

        // Fix any double paragraph tags and clean up
        htmlContent = htmlContent
            .replaceAll("<p></p>", "")
            .replaceAll("<p><h([1-6])>", "<h$1>")
            .replaceAll("</h([1-6])></p>", "</h$1>");

        log.debug("✅ Markdown content formatted successfully ({} characters)", htmlContent.length());
        return htmlContent;
    }
}
