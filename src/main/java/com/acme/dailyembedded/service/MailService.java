package com.acme.dailyembedded.service;

import com.acme.dailyembedded.entity.EmailLog;
import com.acme.dailyembedded.repository.EmailLogRepository;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.ZonedDateTime;

@Service
public class MailService {

  private static final Logger logger = LoggerFactory.getLogger(MailService.class);

  private final JavaMailSender mailSender;
  private final EmailLogRepository emailLogRepository;
  private final Parser markdownParser;
  private final HtmlRenderer htmlRenderer;

  public MailService(JavaMailSender mailSender, EmailLogRepository emailLogRepository) {
    this.mailSender = mailSender;
    this.emailLogRepository = emailLogRepository;

    // Initialize Flexmark for Markdown to HTML conversion
    MutableDataSet options = new MutableDataSet();
    this.markdownParser = Parser.builder(options).build();
    this.htmlRenderer = HtmlRenderer.builder(options).build();
  }

  public void sendEmail(Long lessonId, EmailLog.Type type, String toEmail, String subject,
                       String markdownContent) {
    EmailLog emailLog = new EmailLog(lessonId, type, toEmail);

    try {
      String htmlContent = convertMarkdownToHtml(markdownContent);

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlContent, true); // true = HTML content

      // Add footer
      String fullHtmlContent = addEmailFooter(htmlContent);
      helper.setText(fullHtmlContent, true);

      mailSender.send(message);

      // Log successful send
      emailLog.setStatus(EmailLog.Status.SENT);
      emailLog.setSentAt(ZonedDateTime.now());
      emailLog.setProviderMsgId(message.getMessageID());

      logger.info("Email sent successfully to {} for lesson {}", toEmail, lessonId);

    } catch (Exception e) {
      logger.error("Failed to send email to {} for lesson {}", toEmail, lessonId, e);

      emailLog.setStatus(EmailLog.Status.FAILED);
      emailLog.setError(e.getMessage());
    }

    emailLogRepository.save(emailLog);
  }

  public void sendTestEmail(String toEmail) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(toEmail);
      helper.setSubject("[Test] Daily Embedded Learning - SMTP Test");

      String testContent = """
          <h2>SMTP Test Email</h2>
          <p>This is a test email to verify your SMTP configuration is working correctly.</p>
          <p><strong>Test sent at:</strong> %s</p>
          <hr>
          <p><small>Daily Embedded Learning System</small></p>
          """.formatted(ZonedDateTime.now());

      helper.setText(testContent, true);

      mailSender.send(message);
      logger.info("Test email sent successfully to {}", toEmail);

    } catch (Exception e) {
      logger.error("Failed to send test email to {}", toEmail, e);
      throw new RuntimeException("Test email failed: " + e.getMessage(), e);
    }
  }

  private String convertMarkdownToHtml(String markdown) {
    if (markdown == null || markdown.trim().isEmpty()) {
      return "<p>No content available.</p>";
    }

    Document document = markdownParser.parse(markdown);
    return htmlRenderer.render(document);
  }

  private String addEmailFooter(String htmlContent) {
    String footer = """
        <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
        <div style="font-size: 12px; color: #666; text-align: center; padding: 20px;">
          <p><strong>Daily Embedded Learning</strong></p>
          <p>Quiz answers are provided at the end of each lesson for self-assessment.</p>
          <p>English technical content optimized for embedded systems professionals.</p>
        </div>
        """;

    return htmlContent + footer;
  }

  public String generateDigestSubject(String date, int topicCount) {
    return String.format("[Daily Overview] %s (%d topics)", date, topicCount);
  }

  public String generateDeepDiveSubject(int seq, String topicTitle) {
    return String.format("[Deep Dive #%d] %s", seq, topicTitle);
  }
}
