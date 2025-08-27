package com.knowledge.topic.service;

import com.knowledge.topic.dto.TopicDetail;
import com.knowledge.topic.dto.TopicOverview;
import com.knowledge.topic.model.EmailType;
import com.knowledge.topic.model.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopicEmailService {

    private final JavaMailSender mailSender;
    private final ContentParserService contentParser; // NEW: Add parser service

    private final String fromEmail;
    private final String toEmail;

    private static final DateTimeFormatter EMAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter EMAIL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TopicEmailService(JavaMailSender mailSender,
                           ContentParserService contentParser,
                           @Value("${app.mail-from}") String fromEmail,
                           @Value("${app.mail-to}") String toEmail) {
        this.mailSender = mailSender;
        this.contentParser = contentParser;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
    }

    public void sendOverviewEmail(List<TopicOverview> overviews) {
        if (overviews == null || overviews.isEmpty()) {
            log.warn("No overview content to send via email");
            return;
        }

        try {
            // Create subject with topic names
            String topicNames = overviews.stream()
                    .map(overview -> overview.getTopicName())
                    .collect(Collectors.joining(", "));
            
            String subject;
            if (overviews.size() == 1) {
                subject = "📋 Daily Overview: " + topicNames + " - " + 
                         LocalDateTime.now().format(EMAIL_DATE_FORMATTER);
            } else {
                subject = "📋 Daily Overview: " + overviews.size() + " Topics - " + 
                         LocalDateTime.now().format(EMAIL_DATE_FORMATTER);
            }
            
            String htmlContent = buildOverviewEmailContent(overviews);

            sendEmail(subject, htmlContent);
            log.info("Successfully sent overview email with {} topics", overviews.size());

        } catch (Exception e) {
            log.error("Error sending overview email: {}", e.getMessage(), e);
            // Don't throw exception - allow processing to continue without email
            log.warn("Continuing without sending overview email due to configuration issues");
        }
    }

    public void sendDetailedEmail(List<TopicDetail> details) {
        if (details == null || details.isEmpty()) {
            log.warn("No detailed content to send via email");
            return;
        }

        try {
            // Create subject with topic names
            String topicNames = details.stream()
                    .map(detail -> detail.getTopicName())
                    .collect(Collectors.joining(", "));
            
            String subject;
            if (details.size() == 1) {
                subject = "🔬 Deep Dive: " + topicNames + " - " + 
                         LocalDateTime.now().format(EMAIL_DATE_FORMATTER);
            } else {
                subject = "🔬 Deep Dive: " + details.size() + " Topics - " + 
                         LocalDateTime.now().format(EMAIL_DATE_FORMATTER);
            }
            
            String htmlContent = buildDetailedEmailContent(details);

            sendEmail(subject, htmlContent);
            log.info("Successfully sent detailed email with {} topics", details.size());

        } catch (Exception e) {
            log.error("Error sending detailed email: {}", e.getMessage(), e);
            // Don't throw exception - allow processing to continue without email
            log.warn("Continuing without sending detailed email due to configuration issues");
        }
    }

    public void sendDualTopicEmails(List<Topic> topics,
                                   List<TopicOverview> overviews,
                                   List<TopicDetail> details) {
        log.info("Attempting to send dual topic emails for {} topics", topics.size());

        boolean overviewSent = false;
        boolean detailedSent = false;

        // Try to send overview email
        try {
            if (overviews != null && !overviews.isEmpty()) {
                log.info("Sending overview email for topics: {}", overviews.stream().map(TopicOverview::getTopicName).toList());
                sendOverviewEmail(overviews);
                overviewSent = true;
            } else {
                log.info("No overview content to send via email");
            }
        } catch (Exception e) {
            log.warn("Failed to send overview email, continuing: {}", e.getMessage());
        }

        // Try to send detailed email
        try {
            log.info("Sending detailed email for topics: {}", details.stream().map(TopicDetail::getTopicName).toList());
            log.info("Sending detailed email for topics: {}", details);
            sendDetailedEmail(details);
            detailedSent = true;
        } catch (Exception e) {
            log.warn("Failed to send detailed email, continuing: {}", e.getMessage());
        }

        if (overviewSent || detailedSent) {
            log.info("Successfully sent at least one email type (Overview: {}, Detailed: {})",
                    overviewSent, detailedSent);
        } else {
            log.warn("No emails were sent successfully. Please check email configuration.");
            // Don't throw exception - content was still generated successfully
        }
    }

    private void sendEmail(String subject, String htmlContent) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MailException e) {
            log.error("Mail sending failed: {}. Please check email configuration in application.properties", e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    private String buildOverviewEmailContent(List<TopicOverview> overviews) {
        StringBuilder html = new StringBuilder();
        String currentDate = LocalDateTime.now().format(EMAIL_DATE_FORMATTER);
        String timestamp = LocalDateTime.now().format(EMAIL_TIME_FORMATTER);

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>📋 Daily Topic Overview - %s</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, sans-serif; 
                            line-height: 1.6; 
                            color: #2c3e50; 
                            max-width: 900px; 
                            margin: 0 auto; 
                            padding: 20px; 
                            background-color: #f8f9fa;
                        }
                        .email-container {
                            background: white;
                            border-radius: 12px;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                            overflow: hidden;
                        }
                        .header { 
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                            color: white; 
                            padding: 40px; 
                            text-align: center; 
                        }
                        .header h1 { margin: 0; font-size: 2.2em; font-weight: 300; }
                        .header p { margin: 10px 0 0 0; opacity: 0.9; }
                        
                        .topic-card { 
                            background: #ffffff; 
                            margin: 25px 30px; 
                            border-left: 5px solid #667eea; 
                            border-radius: 8px;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                            overflow: hidden;
                        }
                        .topic-header {
                            background: linear-gradient(135deg, #f8f9fa 0%%, #e9ecef 100%%);
                            padding: 25px;
                            border-bottom: 1px solid #dee2e6;
                        }
                        .topic-title { 
                            color: #2c3e50; 
                            margin: 0 0 10px 0; 
                            font-size: 1.8em;
                            font-weight: 600;
                        }
                        .topic-meta {
                            display: flex;
                            gap: 15px;
                            align-items: center;
                            flex-wrap: wrap;
                        }
                        .level-badge { 
                            background: #28a745; 
                            color: white; 
                            padding: 6px 14px; 
                            border-radius: 20px; 
                            font-size: 0.85em;
                            font-weight: 600;
                            text-transform: uppercase;
                        }
                        .level-advanced { background: #dc3545; }
                        .level-intermediate { background: #ffc107; color: #212529; }
                        .level-beginner { background: #28a745; }
                        
                        .category-badge { 
                            background: #6f42c1; 
                            color: white; 
                            padding: 6px 14px; 
                            border-radius: 20px; 
                            font-size: 0.85em;
                            font-weight: 500;
                        }
                        
                        .topic-content { padding: 30px; }
                        .introduction { 
                            font-size: 1.1em; 
                            line-height: 1.7; 
                            color: #2c3e50; 
                            margin-bottom: 25px;
                            text-align: justify;
                        }
                        
                        .concepts-section, .takeaways-section, .stats-section {
                            background: #f8f9fa;
                            padding: 20px;
                            border-radius: 8px;
                            margin: 20px 0;
                            border-left: 4px solid #667eea;
                        }
                        .concepts-section { border-left-color: #17a2b8; }
                        .takeaways-section { border-left-color: #28a745; }
                        .stats-section { border-left-color: #ffc107; }
                        
                        .section-title {
                            font-size: 1.2em;
                            font-weight: 600;
                            color: #2c3e50;
                            margin: 0 0 15px 0;
                            display: flex;
                            align-items: center;
                            gap: 8px;
                        }
                        
                        .concept-list, .takeaway-list, .stats-list { 
                            list-style: none; 
                            padding: 0; 
                            margin: 0;
                        }
                        .concept-list li, .takeaway-list li, .stats-list li { 
                            padding: 8px 0; 
                            border-bottom: 1px solid #e9ecef;
                            position: relative;
                            padding-left: 20px;
                        }
                        .concept-list li:before {
                            content: "💡";
                            position: absolute;
                            left: 0;
                        }
                        .takeaway-list li:before {
                            content: "✅";
                            position: absolute;
                            left: 0;
                        }
                        .stats-list li:before {
                            content: "📊";
                            position: absolute;
                            left: 0;
                        }
                        .concept-list li:last-child, .takeaway-list li:last-child, .stats-list li:last-child { 
                            border-bottom: none; 
                        }
                        
                        .why-matters {
                            background: linear-gradient(135deg, #e3f2fd 0%%, #bbdefb 100%%);
                            padding: 25px;
                            border-radius: 8px;
                            margin: 20px 0;
                            border-left: 4px solid #2196f3;
                        }
                        .why-matters .icon { font-size: 1.5em; margin-right: 10px; }
                        
                        .relevance-section {
                            margin: 20px 0;
                            padding: 20px;
                            background: #fff3cd;
                            border-radius: 8px;
                            border-left: 4px solid #ffc107;
                        }
                        
                        .footer {
                            background: linear-gradient(135deg, #2c3e50 0%%, #34495e 100%%);
                            color: white;
                            padding: 30px;
                            text-align: center;
                        }
                        .footer a { color: #74b9ff; text-decoration: none; }
                        .footer a:hover { text-decoration: underline; }
                        
                        @media (max-width: 600px) {
                            .topic-card { margin: 15px; }
                            .topic-content { padding: 20px; }
                            .topic-meta { flex-direction: column; align-items: flex-start; }
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1>📋 Daily Topic Overview</h1>
                            <p><strong>Date:</strong> %s | <strong>Topics:</strong> %d</p>
                        </div>
                """.formatted(currentDate, currentDate, overviews.size()));

        for (TopicOverview overview : overviews) {
            // Parse the AI-generated content
            ContentParserService.ParsedOverview parsed = contentParser.parseOverviewContent(overview.getIntroduction());

            html.append(String.format("""
                    <div class="topic-card">
                        <div class="topic-header">
                            <h2 class="topic-title">%s</h2>
                            <div class="topic-meta">
                                <span class="level-badge level-%s">%s Level</span>
                                <span class="category-badge">%s</span>
                                <small>📊 %d words</small>
                            </div>
                        </div>
                        
                        <div class="topic-content">
                            <div class="introduction">%s</div>
                            
                            <div class="concepts-section">
                                <h4 class="section-title">💡 Key Concepts</h4>
                                <ul class="concept-list">%s</ul>
                            </div>
                            
                            <div class="relevance-section">
                                <h4 class="section-title">🌟 Current Relevance</h4>
                                <p>%s</p>
                            </div>
                            
                            <div class="takeaways-section">
                                <h4 class="section-title">🎯 Main Takeaways</h4>
                                <ul class="takeaway-list">%s</ul>
                            </div>
                            
                            <div class="stats-section">
                                <h4 class="section-title">📊 Quick Stats</h4>
                                <ul class="stats-list">%s</ul>
                            </div>
                            
                            <div class="why-matters">
                                <h4 class="section-title"><span class="icon">💼</span>Why It Matters</h4>
                                <p>%s</p>
                            </div>
                        </div>
                    </div>
                    """,
                    overview.getTopicName(),
                    "intermediate", // Default level - could be enhanced to read from topic data
                    "Intermediate", // Display level
                    overview.getCategory(),
                    overview.getWordCount(),
                    formatTextForHtml(parsed.getIntroduction()),
                    parsed.getKeyConcepts().stream()
                            .map(concept -> "<li>" + formatTextForHtml(concept) + "</li>")
                            .reduce("", String::concat),
                    formatTextForHtml(parsed.getCurrentRelevance()),
                    parsed.getMainTakeaways().stream()
                            .map(takeaway -> "<li>" + formatTextForHtml(takeaway) + "</li>")
                            .reduce("", String::concat),
                    parsed.getQuickStats().stream()
                            .map(stat -> "<li>" + formatTextForHtml(stat) + "</li>")
                            .reduce("", String::concat),
                    formatTextForHtml(parsed.getWhyItMatters())));
        }

        html.append(String.format("""
                        <div class="footer">
                            <h3>🚀 Ready for Deep Dive?</h3>
                            <p>Check your <strong>detailed knowledge email</strong> for comprehensive insights, case studies, and expert perspectives!</p>
                            <p style="margin-top: 20px; font-size: 0.9em; opacity: 0.8;">
                                Generated by Topic Knowledge Service • %s<br>
                                <a href="mailto:%s">Questions or feedback?</a>
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, timestamp, fromEmail));

        return html.toString();
    }

    private String buildDetailedEmailContent(List<TopicDetail> details) {
        StringBuilder html = new StringBuilder();
        String currentDate = LocalDateTime.now().format(EMAIL_DATE_FORMATTER);
        String timestamp = LocalDateTime.now().format(EMAIL_TIME_FORMATTER);

        html.append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>🔬 Deep Dive Knowledge - %s</title>
                    <style>
                        body { 
                            font-family: Georgia, serif; 
                            line-height: 1.8; 
                            color: #2c3e50; 
                            max-width: 900px; 
                            margin: 0 auto; 
                            padding: 20px; 
                        }
                        .header { 
                            background: linear-gradient(135deg, #2c3e50 0%%, #34495e 100%%); 
                            color: white; 
                            padding: 40px; 
                            border-radius: 8px; 
                            margin-bottom: 40px; 
                            text-align: center; 
                        }
                        .detail-section { 
                            background: #ffffff; 
                            padding: 30px; 
                            margin: 25px 0; 
                            border: 1px solid #dee2e6; 
                            border-radius: 8px;
                            box-shadow: 0 3px 6px rgba(0,0,0,0.1);
                        }
                        .overview-section { 
                            background: #f8f9fa; 
                            border-left: 4px solid #007bff;
                        }
                        .concepts-section { 
                            background: #e3f2fd; 
                            border-left: 4px solid #2196f3;
                        }
                        .code-examples-section { 
                            background: #f3e5f5; 
                            border-left: 4px solid #9c27b0;
                        }
                        .comparisons-section { 
                            background: #fff3e0; 
                            border-left: 4px solid #ff9800;
                        }
                        .best-practices-section { 
                            background: #e8f5e8; 
                            border-left: 4px solid #4caf50;
                        }
                        .advanced-section { 
                            background: #ffebee; 
                            border-left: 4px solid #f44336;
                        }
                        .related-topics-section { 
                            background: #e1f5fe; 
                            border-left: 4px solid #00bcd4;
                        }
                        .resources-section { 
                            background: #f1f8e9; 
                            border-left: 4px solid #8bc34a;
                        }
                        .examples-grid {
                            display: grid;
                            gap: 20px;
                            margin-top: 15px;
                        }
                        .code-example {
                            background: #1e1e1e;
                            color: #d4d4d4;
                            padding: 20px;
                            border-radius: 8px;
                            margin: 15px 0;
                            font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                        }
                        .code-example h4 {
                            color: #569cd6;
                            margin: 0 0 15px 0;
                            font-size: 1.1em;
                        }
                        .code-example pre {
                            margin: 0;
                            overflow-x: auto;
                            background: transparent;
                            border: none;
                            padding: 0;
                        }
                        .code-example code {
                            background: transparent;
                            color: inherit;
                            font-size: 0.9em;
                            line-height: 1.5;
                        }
                        .explanation {
                            background: #2d2d30;
                            color: #cccccc;
                            padding: 10px;
                            margin-top: 10px;
                            border-radius: 4px;
                            font-size: 0.9em;
                            border-left: 3px solid #007acc;
                        }
                        .concept-item {
                            background: white;
                            padding: 15px;
                            margin: 10px 0;
                            border-radius: 6px;
                            border-left: 3px solid #17a2b8;
                        }
                        .comparison-item {
                            background: white;
                            padding: 15px;
                            margin: 10px 0;
                            border-radius: 6px;
                            border: 1px solid #e9ecef;
                        }
                        pre.language-java {
                            background: #1e1e1e;
                            color: #d4d4d4;
                            padding: 15px;
                            border-radius: 6px;
                            overflow-x: auto;
                            margin: 10px 0;
                            font-size: 0.9em;
                            line-height: 1.4;
                        }
                        .section-title {
                            color: #2c3e50;
                            border-bottom: 3px solid #3498db;
                            padding-bottom: 10px;
                            margin-bottom: 20px;
                        }
                        .topic-header {
                            background: #ecf0f1;
                            padding: 20px;
                            border-radius: 6px;
                            margin-bottom: 20px;
                        }
                        .footer {
                            background: #2c3e50;
                            color: white;
                            padding: 25px;
                            border-radius: 6px;
                            margin-top: 40px;
                            text-align: center;
                        }
                        ul { padding-left: 25px; }
                        li { margin: 10px 0; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>🔬 Deep Dive Knowledge Session</h1>
                        <p><strong>Date:</strong> %s | <strong>In-Depth Topics:</strong> %d</p>
                    </div>
                """.formatted(currentDate, currentDate, details.size()));

        for (TopicDetail detail : details) {
            // Parse the AI-generated content using the content parser service
            ContentParserService.ParsedDetail parsed = contentParser.parseDetailedContent(detail.getComprehensiveIntroduction());
            
            html.append(String.format("""
                    <div class="detail-section">
                        <div class="topic-header">
                            <h2>%s - Comprehensive Programming Guide</h2>
                            <p><strong>Category:</strong> %s | <strong>Word Count:</strong> %d words</p>
                        </div>
                        
                        <div class="overview-section">
                            <h3 class="section-title">� Overview & Fundamentals</h3>
                            %s
                        </div>
                        
                        <div class="concepts-section">
                            <h3 class="section-title">🔧 Key Concepts & Components</h3>
                            %s
                        </div>
                        
                        <div class="code-examples-section">
                            <h3 class="section-title">� Code Examples & Practical Implementation</h3>
                            <div class="examples-grid">%s</div>
                        </div>
                        
                        <div class="comparisons-section">
                            <h3 class="section-title">⚖️ Comparisons & Alternatives</h3>
                            %s
                        </div>
                        
                        <div class="best-practices-section">
                            <h3 class="section-title">✅ Best Practices & Tips</h3>
                            %s
                        </div>
                        
                        <div class="advanced-section">
                            <h3 class="section-title">� Advanced Techniques & Case Studies</h3>
                            <ul>%s</ul>
                        </div>
                        
                        <div class="related-topics-section">
                            <h3 class="section-title">� Related Topics for Further Learning</h3>
                            %s
                        </div>
                        
                        <div class="resources-section">
                            <h3 class="section-title">📖 Learning Resources & References</h3>
                            <ul>%s</ul>
                        </div>
                    </div>
                    """,
                    detail.getTopicName(),
                    detail.getCategory(),
                    detail.getWordCount(),
                    formatTextForHtml(parsed.getExecutiveSummary()),
                    formatTextForHtml(parsed.getCorePrinciples()),
                    parsed.getRealWorldApplications().stream()
                            .map(app -> "<div class='code-example'><h4>" + formatTextForHtml(app.getTitle()) + "</h4>" + 
                                       app.getDescription() + "<div class='explanation'>" + formatTextForHtml(app.getImpact()) + "</div></div>")
                            .reduce("", String::concat),
                    formatTextForHtml(parsed.getFutureOutlook()),
                    formatTextForHtml(parsed.getCurrentInnovation()),
                    parsed.getCaseStudies().stream()
                            .map(study -> "<li><strong>" + formatTextForHtml(study.getCompany()) + ":</strong> " + 
                                         formatTextForHtml(study.getChallenge()) + " → " + formatTextForHtml(study.getSolution()) + 
                                         " <em>Results: " + formatTextForHtml(study.getResults()) + "</em></li>")
                            .reduce("", String::concat),
                    formatTextForHtml(parsed.getInterconnectedConcepts()),
                    parsed.getLearningResources().stream()
                            .map(resource -> "<li><strong>" + formatTextForHtml(resource.getType()) + ":</strong> " + 
                                            formatTextForHtml(resource.getTitle()) + " - " + formatTextForHtml(resource.getRelevance()) + "</li>")
                            .reduce("", String::concat)));
        }

        html.append(String.format("""
                    <div class="footer">
                        <p>🎓 <strong>Knowledge Journey Complete!</strong> You've gained comprehensive understanding of today's topics.</p>
                        <p>Generated by Topic Knowledge Service • Deep Learning Mode • %s</p>
                    </div>
                </body>
                </html>
                """, timestamp));

        return html.toString();
    }

    private String formatTextForHtml(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // Clean up the text and convert to proper HTML
        String formatted = text.trim()
                  // First escape HTML entities
                  .replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  
                  // Convert **bold** to <strong>bold</strong>
                  .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
                  
                  // Convert markdown-style lists to HTML
                  .replaceAll("(?m)^- (.+)$", "<li>$1</li>")
                  .replaceAll("(?m)^\\* (.+)$", "<li>$1</li>")
                  
                  // Convert double newlines to paragraph breaks
                  .replace("\n\n", "</p><p>")
                  
                  // Convert single newlines to breaks
                  .replace("\n", "<br>");
        
        // Wrap in paragraph tags if not already wrapped and doesn't contain list items
        if (!formatted.contains("<li>") && !formatted.startsWith("<p>")) {
            formatted = "<p>" + formatted + "</p>";
        }
        
        // Clean up empty paragraphs and formatting issues
        formatted = formatted
                  .replace("<p></p>", "")
                  .replace("<p><br>", "<p>")
                  .replace("<br></p>", "</p>")
                  .replace("<li></li>", "");
        
        // Wrap consecutive list items in ul tags
        if (formatted.contains("<li>")) {
            formatted = formatted.replaceAll("(<li>.*?</li>)+", "<ul>$0</ul>");
        }
        
        return formatted;
    }
}
