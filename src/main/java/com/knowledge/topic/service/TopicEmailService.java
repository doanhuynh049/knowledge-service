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

@Service
@Slf4j
public class TopicEmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String toEmail;

    private static final DateTimeFormatter EMAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter EMAIL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TopicEmailService(JavaMailSender mailSender,
                           @Value("${app.mail-from}") String fromEmail,
                           @Value("${app.mail-to}") String toEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
    }

    public void sendOverviewEmail(List<TopicOverview> overviews) {
        if (overviews == null || overviews.isEmpty()) {
            log.warn("No overview content to send via email");
            return;
        }

        try {
            String subject = EmailType.OVERVIEW.getSubject() + " - " +
                           LocalDateTime.now().format(EMAIL_DATE_FORMATTER);
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
            String subject = EmailType.DETAILED.getSubject() + " - " +
                           LocalDateTime.now().format(EMAIL_DATE_FORMATTER);
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
            sendOverviewEmail(overviews);
            overviewSent = true;
        } catch (Exception e) {
            log.warn("Failed to send overview email, continuing: {}", e.getMessage());
        }

        // Try to send detailed email
        try {
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
                            color: #333; 
                            max-width: 800px; 
                            margin: 0 auto; 
                            padding: 20px; 
                        }
                        .header { 
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                            color: white; 
                            padding: 30px; 
                            border-radius: 8px; 
                            margin-bottom: 30px; 
                            text-align: center; 
                        }
                        .overview-card { 
                            background: #f8f9fa; 
                            padding: 25px; 
                            margin: 20px 0; 
                            border-left: 4px solid #007bff; 
                            border-radius: 8px;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        }
                        .takeaways { 
                            background: #e8f4fd; 
                            padding: 20px; 
                            border-radius: 6px;
                            margin: 15px 0;
                        }
                        .category-badge { 
                            background: #28a745; 
                            color: white; 
                            padding: 6px 12px; 
                            border-radius: 15px; 
                            font-size: 0.85em;
                            font-weight: bold;
                        }
                        .topic-title {
                            color: #2c3e50;
                            margin-bottom: 15px;
                            border-bottom: 2px solid #ecf0f1;
                            padding-bottom: 10px;
                        }
                        .introduction {
                            margin: 15px 0;
                            color: #2c3e50;
                        }
                        .footer {
                            background: #f1f3f4;
                            padding: 20px;
                            border-radius: 6px;
                            margin-top: 30px;
                            text-align: center;
                            color: #666;
                        }
                        ul { padding-left: 20px; }
                        li { margin: 8px 0; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>📋 Daily Topic Overview</h1>
                        <p><strong>Date:</strong> %s | <strong>Topics:</strong> %d</p>
                    </div>
                """.formatted(currentDate, currentDate, overviews.size()));

        for (TopicOverview overview : overviews) {
            html.append(String.format("""
                    <div class="overview-card">
                        <h2 class="topic-title">%s <span class="category-badge">%s</span></h2>
                        <div class="introduction">%s</div>
                        <div class="takeaways">
                            <h4>🎯 Key Takeaways:</h4>
                            <ul>%s</ul>
                        </div>
                        <p><em>💡 <strong>Why it matters:</strong> %s</em></p>
                        <small>📊 Word count: %d words</small>
                    </div>
                    """,
                    overview.getTopicName(),
                    overview.getCategory(),
                    formatTextForHtml(overview.getIntroduction()),
                    overview.getMainTakeaways().stream()
                            .map(takeaway -> "<li>" + formatTextForHtml(takeaway) + "</li>")
                            .reduce("", String::concat),
                    formatTextForHtml(overview.getWhyItMatters()),
                    overview.getWordCount()));
        }

        html.append(String.format("""
                    <div class="footer">
                        <p>📚 <strong>Want deeper knowledge?</strong> Check your detailed knowledge email for comprehensive insights!</p>
                        <p>Generated by Topic Knowledge Service • %s</p>
                    </div>
                </body>
                </html>
                """, timestamp));

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
                        .historical-context { 
                            background: #fff3cd; 
                            border-left: 4px solid #ffc107;
                        }
                        .case-study { 
                            background: #d1ecf1; 
                            border-left: 4px solid #17a2b8;
                        }
                        .expert-insight { 
                            font-style: italic; 
                            background: #e2e3e5; 
                            padding: 20px; 
                            border-left: 4px solid #6c757d; 
                            margin: 20px 0;
                        }
                        .further-reading { 
                            background: #d4edda; 
                            border-left: 4px solid #28a745;
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
            html.append(String.format("""
                    <div class="detail-section">
                        <div class="topic-header">
                            <h2>%s - Comprehensive Analysis</h2>
                            <p><strong>Category:</strong> %s | <strong>Word Count:</strong> %d words</p>
                        </div>
                        
                        <div class="historical-context">
                            <h3 class="section-title">📜 Historical Context</h3>
                            %s
                        </div>
                        
                        <h3 class="section-title">🔧 Core Mechanisms & Principles</h3>
                        %s
                        
                        <div class="case-study">
                            <h3 class="section-title">📊 Real-World Applications & Case Studies</h3>
                            <ul>%s</ul>
                            <p><strong>Case Studies:</strong></p>
                            <ul>%s</ul>
                        </div>
                        
                        <h3 class="section-title">🌐 Related Concepts & Connections</h3>
                        %s
                        
                        <div class="expert-insight">
                            <h3 class="section-title">💭 Expert Insights</h3>
                            %s
                        </div>
                        
                        <h3 class="section-title">🔮 Future Outlook & Trends</h3>
                        %s
                        
                        <div class="further-reading">
                            <h3 class="section-title">📖 Further Learning Resources</h3>
                            <ul>%s</ul>
                        </div>
                    </div>
                    """,
                    detail.getTopicName(),
                    detail.getCategory(),
                    detail.getWordCount(),
                    formatTextForHtml(detail.getHistoricalContext()),
                    formatTextForHtml(detail.getCoreMechanisms()),
                    detail.getRealWorldApplications().stream()
                            .map(app -> "<li>" + formatTextForHtml(app) + "</li>")
                            .reduce("", String::concat),
                    detail.getCaseStudies().stream()
                            .map(study -> "<li>" + formatTextForHtml(study) + "</li>")
                            .reduce("", String::concat),
                    formatTextForHtml(detail.getRelatedConcepts()),
                    formatTextForHtml(detail.getExpertInsights()),
                    formatTextForHtml(detail.getFutureOutlook()),
                    detail.getFurtherLearningResources().stream()
                            .map(resource -> "<li>" + formatTextForHtml(resource) + "</li>")
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
        if (text == null) return "";
        return text.replace("\n", "<br>")
                  .replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;");
    }
}
