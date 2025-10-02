package com.knowledge.learning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledge.learning.model.LearningDay;
import lombok.RequiredArgsConstructor;
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
public class LearningEmailService {

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
    public void sendStructuredLearningEmail(LearningDay learningDay, String content) {
        log.info("📧 Preparing structured learning email for Day {}: {}",
            learningDay.getDay(), learningDay.getPhase());

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
                learningDay.getDay(), learningDay.getPhase());
                
        } catch (Exception e) {
            log.error("❌ Failed to send structured learning email for Day {}: {}",
                learningDay.getDay(), e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    /**
     * Load HTML template and replace placeholders
     */
    private String loadAndProcessTemplate(LearningDay learningDay, String content) throws IOException {
        log.debug("🎨 Loading and processing HTML template for Day {}", learningDay.getDay());

        // Load the HTML template
        ClassPathResource resource = new ClassPathResource("learning-path-email-template.html");
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // Replace placeholders with actual data
        String processedHtml = template
            .replace("{{DAY}}", String.valueOf(learningDay.getDay()))
            .replace("{{WEEK}}", learningDay.getWeek())
            .replace("{{PHASE}}", learningDay.getPhase())
            .replace("{{ALGORITHM_TASK}}", learningDay.getAlgorithmTask())
            .replace("{{THEORY_TASK}}", learningDay.getTheoryTask())
            .replace("{{CODING_TASK}}", learningDay.getCodingTask())
            .replace("{{REFLECTION_TASK}}", learningDay.getReflectionTask())
            .replace("{{CONTENT}}", parseJsonToStructuredHtml(content))
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
     * Parse content (either JSON or HTML) and convert to structured HTML with proper containers
     */
    private String parseJsonToStructuredHtml(String content) {
        log.debug("🔧 Processing content for email template insertion");

        if (content == null || content.trim().isEmpty()) {
            log.warn("⚠️ Empty content provided");
            return "<div class=\"content-sections-grid\"><p><em>No content available.</em></p></div>";
        }

        // Check if content is HTML (complete document or fragments)
        if (isHtmlContent(content)) {
            log.debug("📄 Detected HTML content, extracting body content for email");
            return extractAndFormatHtmlContent(content);
        }

        // Try to parse as JSON first
        try {
            String cleanJson = cleanJsonContent(content);
            JsonNode rootNode = objectMapper.readTree(cleanJson);
            StringBuilder htmlBuilder = new StringBuilder();

            // Add content sections grid container
            htmlBuilder.append("<div class=\"content-sections-grid\">\n");

            // Process each section with enhanced formatting and better separation
            processSection(htmlBuilder, rootNode, "dailyOverview", "daily-overview-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "algorithmGuidance", "algorithm-guidance-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "theoryExplanation", "theory-explanation-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "codingExercises", "coding-exercises-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "reflectionPrompts", "reflection-prompts-container");
            addContentSeparator(htmlBuilder);

            processSection(htmlBuilder, rootNode, "resourcesAndNext", "resources-next-container");

            // Close content sections grid
            htmlBuilder.append("</div>\n");

            String result = htmlBuilder.toString();
            log.debug("✅ JSON content parsed successfully with enhanced formatting ({} characters)", result.length());
            return result;

        } catch (Exception e) {
            log.debug("📝 Content is not valid JSON, treating as HTML/text: {}", e.getMessage());
            // Fallback to HTML or markdown formatting
            return formatContentAsHtml(content);
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
            case "algorithmGuidance":
                // Wrap algorithm steps in special blocks
                content = content.replaceAll("<strong>([^<]+)</strong>:",
                    "<div class=\"algorithm-block\"><strong>$1</strong>:");
                break;

            case "codingExercises":
                // Wrap code examples in special blocks
                content = content.replaceAll("<strong>([^<]+):</strong>",
                    "<div class=\"coding-block\"><strong>$1:</strong>");
                break;

            case "reflectionPrompts":
                // Wrap reflection questions in special blocks
                content = content.replaceAll("<li>([^<]+)</li>",
                    "<div class=\"reflection-block\"><li>$1</li></div>");
                break;

            case "resourcesAndNext":
                // Enhance resources with special formatting
                content = content.replaceAll("<li>([^<]+)</li>",
                    "<div class=\"resource-block\"><li>$1</li></div>");
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
     * Check if content is HTML by looking for HTML tags
     */
    private boolean isHtmlContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = content.trim();
        return trimmed.contains("<!DOCTYPE html>") || 
               trimmed.contains("<html") || 
               trimmed.contains("<body") || 
               (trimmed.contains("<") && trimmed.contains(">") && 
                (trimmed.contains("<h1>") || trimmed.contains("<h2>") || 
                 trimmed.contains("<section") || trimmed.contains("<div")));
    }

    /**
     * Extract content from HTML document and format for email template
     */
    private String extractAndFormatHtmlContent(String htmlContent) {
        log.debug("🔄 Extracting and formatting HTML content for email template");
        
        try {
            // If it's a complete HTML document, extract the body content
            if (htmlContent.contains("<body")) {
                int bodyStart = htmlContent.indexOf("<body");
                int bodyContentStart = htmlContent.indexOf(">", bodyStart) + 1;
                int bodyEnd = htmlContent.lastIndexOf("</body>");
                
                if (bodyContentStart > 0 && bodyEnd > bodyContentStart) {
                    String bodyContent = htmlContent.substring(bodyContentStart, bodyEnd);
                    log.debug("📤 Extracted body content ({} characters)", bodyContent.length());
                    return enhanceHtmlForEmailTemplate(bodyContent);
                }
            }
            
            // If it contains main content, extract that
            if (htmlContent.contains("<main")) {
                int mainStart = htmlContent.indexOf("<main");
                int mainContentStart = htmlContent.indexOf(">", mainStart) + 1;
                int mainEnd = htmlContent.lastIndexOf("</main>");
                
                if (mainContentStart > 0 && mainEnd > mainContentStart) {
                    String mainContent = htmlContent.substring(mainContentStart, mainEnd);
                    log.debug("📤 Extracted main content ({} characters)", mainContent.length());
                    return enhanceHtmlForEmailTemplate(mainContent);
                }
            }
            
            // Otherwise, use the content as-is but enhance it
            log.debug("📤 Using full HTML content with enhancements");
            return enhanceHtmlForEmailTemplate(htmlContent);
            
        } catch (Exception e) {
            log.error("❌ Error extracting HTML content: {}", e.getMessage(), e);
            return formatContentAsHtml(htmlContent);
        }
    }

    /**
     * Enhance HTML content for better email template integration
     */
    private String enhanceHtmlForEmailTemplate(String htmlContent) {
        log.debug("✨ Enhancing HTML content for email template integration");
        
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("<div class=\"ai-generated-content\">\n");
        
        // Clean up the HTML content
        String cleanHtml = htmlContent
            .replaceAll("<!DOCTYPE[^>]*>", "")
            .replaceAll("<html[^>]*>", "")
            .replaceAll("</html>", "")
            .replaceAll("<head>.*?</head>", "")
            .replaceAll("<body[^>]*>", "")
            .replaceAll("</body>", "")
            .replaceAll("<header[^>]*>.*?</header>", "")
            .replaceAll("<footer[^>]*>.*?</footer>", "")
            .trim();
        
        // Add the cleaned content with email-specific enhancements
        enhanced.append(addEmailSpecificStyles(cleanHtml));
        enhanced.append("</div>\n");
        
        String result = enhanced.toString();
        log.debug("✅ HTML content enhanced for email template ({} characters)", result.length());
        return result;
    }

    /**
     * Add email-specific CSS classes and styling to HTML content
     */
    private String addEmailSpecificStyles(String htmlContent) {
        return htmlContent
            .replaceAll("<section([^>]*)class=\"([^\"]*)\"([^>]*)>", 
                       "<section$1class=\"$2 email-section\"$3>")
            .replaceAll("<section(?![^>]*class)", "<section class=\"email-section\"")
            .replaceAll("<div([^>]*)class=\"block\"([^>]*)>", 
                       "<div$1class=\"block email-content-block\"$2>")
            .replaceAll("<h1([^>]*)>", "<h1$1 class=\"email-h1\">")
            .replaceAll("<h2([^>]*)>", "<h2$1 class=\"email-h2\">")
            .replaceAll("<h3([^>]*)>", "<h3$1 class=\"email-h3\">")
            .replaceAll("<pre([^>]*)>", "<pre$1 class=\"email-code-block\">");
    }

    /**
     * Format content as HTML when it's not valid JSON or HTML
     */
    private String formatContentAsHtml(String content) {
        log.debug("🔄 Formatting content as HTML");
        
        if (content == null || content.trim().isEmpty()) {
            return "<div class=\"content-sections-grid\"><p><em>No content available.</em></p></div>";
        }
        
        // If it's already HTML, enhance it
        if (isHtmlContent(content)) {
            return enhanceHtmlForEmailTemplate(content);
        }
        
        // Otherwise, convert markdown-style formatting to HTML
        return formatMarkdownToHtml(content);
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
