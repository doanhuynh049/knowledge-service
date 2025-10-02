package com.knowledge.learning.service;

import com.knowledge.learning.model.LearningDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningEmlFileService {

    @Value("${app.eml-output-directory:eml_files}")
    private String emlOutputDirectory;

    @Value("${app.mail-from}")
    private String fromEmail;

    @Value("${app.mail-to}")
    private String toEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Create EML file from HTML content with proper email formatting
     */
    public File createEmlFile(LearningDay learningDay, String htmlContent) {
        log.info("📧 Creating EML file for Day {}: {}", learningDay.getDay(), learningDay.getPhase());

        try {
            // Create output directory if it doesn't exist
            Path outputDir = Paths.get(emlOutputDirectory);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                log.info("📁 Created EML output directory: {}", outputDir.toAbsolutePath());
            }

            // Generate filename
            String timestamp = LocalDateTime.now().format(FILE_DATE_FORMATTER);
            String filename = String.format("Day%02d_Problems_%s.eml", learningDay.getDay(), timestamp);
            File emlFile = new File(outputDir.toFile(), filename);

            // Create EML content
            String emlContent = generateEmlContent(learningDay, htmlContent);

            // Write to file
            try (FileWriter writer = new FileWriter(emlFile, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(emlContent);
            }

            log.info("✅ EML file created successfully: {} ({} bytes)", 
                emlFile.getAbsolutePath(), emlFile.length());

            return emlFile;

        } catch (Exception e) {
            log.error("❌ Error creating EML file for Day {}: {}", learningDay.getDay(), e.getMessage(), e);
            throw new RuntimeException("Failed to create EML file", e);
        }
    }

    /**
     * Generate complete EML content with proper headers and HTML body
     */
    private String generateEmlContent(LearningDay learningDay, String htmlContent) {
        log.debug("🔧 Generating EML content for Day {}", learningDay.getDay());

        StringBuilder eml = new StringBuilder();

        // EML Headers
        String subject = String.format("🎯 Day %d Programming Challenges - %s", learningDay.getDay(), learningDay.getPhase());
        String messageId = generateMessageId(learningDay);
        String currentDateTime = java.time.ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);

        eml.append("From: ").append(fromEmail).append("\r\n");
        eml.append("To: ").append(toEmail).append("\r\n");
        eml.append("Subject: ").append(subject).append("\r\n");
        eml.append("Date: ").append(currentDateTime).append("\r\n");
        eml.append("Message-ID: ").append(messageId).append("\r\n");
        eml.append("MIME-Version: 1.0\r\n");
        eml.append("Content-Type: text/html; charset=UTF-8\r\n");
        eml.append("Content-Transfer-Encoding: 8bit\r\n");
        eml.append("X-Mailer: 6-Month Learning Path Service\r\n");
        eml.append("X-Learning-Day: ").append(learningDay.getDay()).append("\r\n");
        eml.append("X-Learning-Phase: ").append(learningDay.getPhase()).append("\r\n");
        eml.append("X-Priority: 1\r\n");
        eml.append("Importance: High\r\n");
        eml.append("\r\n"); // Empty line separating headers from body

        // Add HTML body
        eml.append(htmlContent);

        log.debug("✅ EML content generated ({} characters)", eml.length());
        return eml.toString();
    }

    /**
     * Generate unique message ID for the email
     */
    private String generateMessageId(LearningDay learningDay) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String domain = fromEmail.contains("@") ? fromEmail.substring(fromEmail.indexOf("@") + 1) : "learning-path.local";
        return String.format("<%s.day%d.%s@%s>", timestamp, learningDay.getDay(), learningDay.getPhase().toLowerCase(), domain);
    }

    /**
     * Create EML file with Base64 encoded content (alternative format)
     */
    public File createBase64EmlFile(LearningDay learningDay, String htmlContent) {
        log.info("📧 Creating Base64 EML file for Day {}: {}", learningDay.getDay(), learningDay.getPhase());

        try {
            Path outputDir = Paths.get(emlOutputDirectory);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            String timestamp = LocalDateTime.now().format(FILE_DATE_FORMATTER);
            String filename = String.format("Day%02d_Problems_B64_%s.eml", learningDay.getDay(), timestamp);
            File emlFile = new File(outputDir.toFile(), filename);

            String emlContent = generateBase64EmlContent(learningDay, htmlContent);

            try (FileWriter writer = new FileWriter(emlFile, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(emlContent);
            }

            log.info("✅ Base64 EML file created: {} ({} bytes)", emlFile.getAbsolutePath(), emlFile.length());
            return emlFile;

        } catch (Exception e) {
            log.error("❌ Error creating Base64 EML file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Base64 EML file", e);
        }
    }

    /**
     * Generate EML content with Base64 encoding
     */
    private String generateBase64EmlContent(LearningDay learningDay, String htmlContent) {
        StringBuilder eml = new StringBuilder();

        String subject = String.format("🎯 Day %d Programming Challenges - %s", learningDay.getDay(), learningDay.getPhase());
        String messageId = generateMessageId(learningDay);
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);

        // Encode HTML content in Base64
        String encodedContent = Base64.getEncoder().encodeToString(htmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
        // Split Base64 content into 76-character lines (RFC requirement)
        String wrappedContent = wrapBase64Content(encodedContent);

        eml.append("From: ").append(fromEmail).append("\r\n");
        eml.append("To: ").append(toEmail).append("\r\n");
        eml.append("Subject: ").append(subject).append("\r\n");
        eml.append("Date: ").append(currentDateTime).append("\r\n");
        eml.append("Message-ID: ").append(messageId).append("\r\n");
        eml.append("MIME-Version: 1.0\r\n");
        eml.append("Content-Type: text/html; charset=UTF-8\r\n");
        eml.append("Content-Transfer-Encoding: base64\r\n");
        eml.append("X-Mailer: 6-Month Learning Path Service\r\n");
        eml.append("X-Learning-Day: ").append(learningDay.getDay()).append("\r\n");
        eml.append("X-Learning-Phase: ").append(learningDay.getPhase()).append("\r\n");
        eml.append("\r\n");
        eml.append(wrappedContent);

        return eml.toString();
    }

    /**
     * Wrap Base64 content to 76-character lines as per RFC requirements
     */
    private String wrapBase64Content(String base64Content) {
        StringBuilder wrapped = new StringBuilder();
        int lineLength = 76;
        
        for (int i = 0; i < base64Content.length(); i += lineLength) {
            int end = Math.min(i + lineLength, base64Content.length());
            wrapped.append(base64Content, i, end).append("\r\n");
        }
        
        return wrapped.toString();
    }

    /**
     * Get EML files directory path
     */
    public String getEmlOutputDirectory() {
        return emlOutputDirectory;
    }

    /**
     * Clean up old EML files (keep only last 30 days)
     */
    public void cleanupOldEmlFiles() {
        log.info("🧹 Cleaning up old EML files...");

        try {
            Path outputDir = Paths.get(emlOutputDirectory);
            if (!Files.exists(outputDir)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 days ago

            Files.list(outputDir)
                .filter(path -> path.toString().endsWith(".eml"))
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("🗑️ Deleted old EML file: {}", path.getFileName());
                    } catch (IOException e) {
                        log.warn("⚠️ Failed to delete old EML file: {}", path.getFileName());
                    }
                });

            log.info("✅ EML cleanup completed");

        } catch (Exception e) {
            log.error("❌ Error during EML cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Validate EML file format
     */
    public boolean validateEmlFile(File emlFile) {
        if (!emlFile.exists() || !emlFile.canRead()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(emlFile))) {
            String firstLine = reader.readLine();
            
            // Check if it starts with proper email headers
            return firstLine != null && 
                   (firstLine.startsWith("From:") || 
                    firstLine.startsWith("Return-Path:") ||
                    firstLine.startsWith("Received:"));

        } catch (IOException e) {
            log.error("❌ Error validating EML file: {}", e.getMessage());
            return false;
        }
    }
}
