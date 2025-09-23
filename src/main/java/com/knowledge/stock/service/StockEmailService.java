package com.knowledge.stock.service;

import com.knowledge.stock.model.StockLearningDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail-from}")

    private String fromEmail;

    @Value("${app.mail-to}")
    private String toEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Send structured learning email with comprehensive logging
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
            String htmlContent = createStructuredLearningEmailTemplate(learningDay, content);

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
     * Create structured HTML email template for learning days
     */
    private String createStructuredLearningEmailTemplate(StockLearningDay learningDay, String content) {
        log.debug("🎨 Creating structured email template for Day {}", learningDay.getDay());

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Day %d Stock Learning</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 900px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f8f9fa;
                    }
                    .container {
                        background-color: white;
                        border-radius: 15px;
                        padding: 30px;
                        box-shadow: 0 4px 20px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #2196F3 0%%, #1976D2 100%%);
                        color: white;
                        padding: 25px;
                        border-radius: 12px;
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .day-badge {
                        background-color: #FF5722;
                        color: white;
                        padding: 8px 16px;
                        border-radius: 20px;
                        font-size: 14px;
                        font-weight: bold;
                        display: inline-block;
                        margin-bottom: 10px;
                    }
                    .phase-badge {
                        background-color: #4CAF50;
                        color: white;
                        padding: 4px 12px;
                        border-radius: 15px;
                        font-size: 12px;
                        display: inline-block;
                        margin-left: 10px;
                    }
                    .learning-goal {
                        background: linear-gradient(135deg, #E3F2FD 0%%, #BBDEFB 100%%);
                        border-left: 5px solid #2196F3;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 8px;
                    }
                    .practice-task {
                        background: linear-gradient(135deg, #FFF3E0 0%%, #FFE0B2 100%%);
                        border-left: 5px solid #FF9800;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 8px;
                    }
                    .content {
                        font-size: 16px;
                        line-height: 1.8;
                        margin: 25px 0;
                    }
                    .content h1, .content h2, .content h3 {
                        color: #1976D2;
                        margin-top: 30px;
                        margin-bottom: 15px;
                    }
                    .content h1 { 
                        font-size: 24px; 
                        border-bottom: 3px solid #2196F3;
                        padding-bottom: 10px;
                    }
                    .content h2 { 
                        font-size: 20px;
                        color: #FF5722;
                    }
                    .content h3 { font-size: 18px; }
                    .content strong {
                        color: #E65100;
                        font-weight: 600;
                    }
                    .content ul, .content ol {
                        padding-left: 25px;
                    }
                    .content li {
                        margin-bottom: 10px;
                    }
                    .progress-section {
                        background: linear-gradient(135deg, #E8F5E8 0%%, #C8E6C9 100%%);
                        border-radius: 10px;
                        padding: 20px;
                        margin: 25px 0;
                        text-align: center;
                    }
                    .next-steps {
                        background: linear-gradient(135deg, #F3E5F5 0%%, #E1BEE7 100%%);
                        border-radius: 10px;
                        padding: 20px;
                        margin: 25px 0;
                    }
                    .footer {
                        margin-top: 40px;
                        padding: 25px;
                        background: linear-gradient(135deg, #ECEFF1 0%%, #CFD8DC 100%%);
                        border-radius: 12px;
                        font-size: 14px;
                        color: #546E7A;
                        text-align: center;
                    }
                    .disclaimer {
                        background-color: #FFECB3;
                        border: 2px solid #FFC107;
                        border-radius: 10px;
                        padding: 20px;
                        margin: 25px 0;
                        font-size: 14px;
                        color: #E65100;
                    }
                    .timestamp {
                        color: #78909C;
                        font-size: 12px;
                        text-align: right;
                        margin-top: 15px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="day-badge">Day %d</div>
                        <div class="phase-badge">%s</div>
                        <h1>📈 %s</h1>
                        <p><strong>%s</strong></p>
                    </div>
                    
                    <div class="learning-goal">
                        <h3>🎯 Today's Learning Goal</h3>
                        <p><strong>%s</strong></p>
                    </div>
                    
                    <div class="content">
                        %s
                    </div>
                    
                    <div class="practice-task">
                        <h3>💼 Your Practice Task</h3>
                        <p><strong>%s</strong></p>
                    </div>
                    
                    <div class="progress-section">
                        <h3>📊 Learning Journey Progress</h3>
                        <p>You're building your stock market knowledge step by step!</p>
                        <p><em>Each day brings you closer to confident investing</em></p>
                    </div>
                    
                    <div class="next-steps">
                        <h3>🚀 Action Steps</h3>
                        <ol>
                            <li><strong>Study (20 minutes):</strong> Read through today's content carefully</li>
                            <li><strong>Apply (15 minutes):</strong> Complete the practice task above</li>
                            <li><strong>Reflect:</strong> Write down 3 key insights from your analysis</li>
                            <li><strong>Decide:</strong> Make a simple investment decision based on your analysis</li>
                        </ol>
                    </div>
                    
                    <div class="disclaimer">
                        <strong>⚠️ Important Educational Disclaimer:</strong> This content is for educational purposes only. 
                        Always conduct your own research and consider consulting with licensed financial advisors before making investment decisions. 
                        Practice with virtual portfolios before risking real money.
                    </div>
                    
                    <div class="footer">
                        <p>📚 <strong>Stock Learning Curriculum</strong> - Day %d of 20</p>
                        <p>🎓 Building Your Investment Knowledge Daily</p>
                        <p>💡 <em>Consistency in learning leads to confidence in investing</em></p>
                        <div class="timestamp">Generated on: %s</div>
                    </div>
                </div>
            </body>
            </html>
            """,
            learningDay.getDay(),                    // Title Day
            learningDay.getDay(),                    // Badge Day
            learningDay.getPhase(),                  // Phase
            learningDay.getWeek(),                   // Week
            learningDay.getTopic(),                  // Topic
            learningDay.getLearningGoal(),           // Learning Goal
            formatContentForHtml(content),           // Main content
            learningDay.getPracticeTask(),           // Practice Task
            learningDay.getDay(),                    // Footer Day
            LocalDateTime.now().format(DATE_FORMATTER) // Timestamp
        );
    }

    /**
     * Format content for HTML display with enhanced formatting
     */
    private String formatContentForHtml(String content) {
        log.debug("🎨 Formatting content for HTML display");

        if (content == null || content.trim().isEmpty()) {
            return "<p>No content available.</p>";
        }

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

        log.debug("✅ Content formatted successfully ({} characters)", htmlContent.length());
        return htmlContent;
    }
}
