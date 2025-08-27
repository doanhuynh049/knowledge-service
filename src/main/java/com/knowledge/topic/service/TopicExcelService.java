package com.knowledge.topic.service;

import com.knowledge.topic.model.Topic;
import com.knowledge.topic.model.TopicStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TopicExcelService {

    @Value("${app.topics-excel-path}")
    private String topicsExcelPath;

    @Value("${app.knowledge-log-path}")
    private String knowledgeLogPath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Topic> getUnprocessedTopics(int limit) {
        List<Topic> topics = new ArrayList<>();
        File file = new File(topicsExcelPath);

        if (!file.exists()) {
            log.warn("Topics Excel file not found: {}", topicsExcelPath);
            createSampleTopicsFile();
            return getDefaultTopics(limit);
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                if (rowCount >= limit) break;

                Topic topic = parseTopicFromRow(row);
                if (topic != null && topic.getStatus() == TopicStatus.NEW) {
                    topics.add(topic);
                    rowCount++;
                }
            }

            log.info("Loaded {} unprocessed topics from Excel file", topics.size());

        } catch (IOException e) {
            log.error("Error reading topics Excel file: {}", e.getMessage(), e);
            return getDefaultTopics(limit);
        }

        return topics;
    }

    public Topic getNextUnprocessedTopic() {
        List<Topic> topics = getUnprocessedTopics(1);
        return topics.isEmpty() ? null : topics.get(0);
    }

    public List<Topic> getTopicsByPriority(int limit) {
        // This would sort by priority - for now, return unprocessed topics
        return getUnprocessedTopics(limit);
    }

    public List<Topic> getTopicsNotProcessedSince(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<Topic> topics = new ArrayList<>();
        File file = new File(topicsExcelPath);

        if (!file.exists()) {
            return topics;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                Topic topic = parseTopicFromRow(row);
                if (topic != null &&
                    (topic.getLastProcessed() == null || topic.getLastProcessed().isBefore(cutoffDate))) {
                    topics.add(topic);
                }
            }

        } catch (IOException e) {
            log.error("Error reading topics Excel file: {}", e.getMessage(), e);
        }

        return topics;
    }

    public void markTopicAsDone(Topic topic) {
        markTopicsAsProcessed(List.of(topic));
    }

    public void markTopicsAsProcessed(List<Topic> topics) {
        File file = new File(topicsExcelPath);

        if (!file.exists()) {
            log.warn("Topics Excel file not found for updating: {}", topicsExcelPath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            LocalDateTime now = LocalDateTime.now();

            for (Topic topic : topics) {
                updateTopicInSheet(sheet, topic, now);
            }

            // Write back to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            log.info("Updated {} topics as processed in Excel file", topics.size());

        } catch (IOException e) {
            log.error("Error updating topics Excel file: {}", e.getMessage(), e);
        }
    }

    public void addTopics(List<Topic> topics) {
        File file = new File(topicsExcelPath);
        Workbook workbook;
        Sheet sheet;

        try {
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Topics");
                createHeaderRow(sheet);
            }

            int lastRowNum = sheet.getLastRowNum();

            for (Topic topic : topics) {
                Row row = sheet.createRow(++lastRowNum);
                populateTopicRow(row, topic);
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            workbook.close();
            log.info("Added {} topics to Excel file", topics.size());

        } catch (IOException e) {
            log.error("Error adding topics to Excel file: {}", e.getMessage(), e);
        }
    }

    public void logProcessedContent(Topic topic, String overviewContent, String detailedContent) {
        File file = new File(knowledgeLogPath);
        Workbook workbook;
        Sheet sheet;

        try {
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Knowledge Log");
                createLogHeaderRow(sheet);
            }

            int lastRowNum = sheet.getLastRowNum();
            Row row = sheet.createRow(lastRowNum + 1);

            row.createCell(0).setCellValue(LocalDateTime.now().format(DATE_FORMATTER));
            row.createCell(1).setCellValue(topic.getName());
            row.createCell(2).setCellValue(topic.getCategory());
            row.createCell(3).setCellValue(overviewContent != null ? overviewContent.split("\\s+").length : 0);
            row.createCell(4).setCellValue(detailedContent != null ? detailedContent.split("\\s+").length : 0);
            row.createCell(5).setCellValue("SUCCESS");
            row.createCell(6).setCellValue("Generated successfully");

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            workbook.close();
            log.info("Logged processed content for topic: {}", topic.getName());

        } catch (IOException e) {
            log.error("Error logging processed content: {}", e.getMessage(), e);
        }
    }

    private Topic parseTopicFromRow(Row row) {
        try {
            String topicLevel = getCellValueAsString(row.getCell(0)); // NEW: Topic Level
            String name = getCellValueAsString(row.getCell(1));
            String category = getCellValueAsString(row.getCell(2));
            int priority = (int) getCellValueAsNumber(row.getCell(3));
            String statusStr = getCellValueAsString(row.getCell(4));
            String description = getCellValueAsString(row.getCell(5));

            if (name == null || name.trim().isEmpty()) {
                return null;
            }

            Topic topic = new Topic(name.trim(), category != null ? category.trim() : "General",
                                  topicLevel != null ? topicLevel.trim() : "Intermediate",
                                  priority > 0 ? priority : 3, description);

            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    // Map Excel statuses to our enum
                    String mappedStatus = mapExcelStatusToEnum(statusStr.trim());
                    topic.setStatus(TopicStatus.valueOf(mappedStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown topic status: {}, defaulting to NEW", statusStr);
                    topic.setStatus(TopicStatus.NEW);
                }
            }

            return topic;

        } catch (Exception e) {
            log.error("Error parsing topic from row: {}", e.getMessage());
            return null;
        }
    }

    private String mapExcelStatusToEnum(String excelStatus) {
        return switch (excelStatus.toUpperCase()) {
            case "OPEN" -> "NEW";
            case "IN_PROGRESS" -> "PROCESSING";
            case "COMPLETED", "DONE" -> "DONE";
            case "CANCELLED", "ARCHIVED" -> "ARCHIVED";
            case "ERROR", "FAILED" -> "ERROR";
            default -> "NEW";
        };
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Topic Level");
        headerRow.createCell(1).setCellValue("Topic Name");
        headerRow.createCell(2).setCellValue("Category");
        headerRow.createCell(3).setCellValue("Priority");
        headerRow.createCell(4).setCellValue("Status");
        headerRow.createCell(5).setCellValue("Description");
        headerRow.createCell(6).setCellValue("Last Processed");
    }

    private void createLogHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Date");
        headerRow.createCell(1).setCellValue("Topic");
        headerRow.createCell(2).setCellValue("Category");
        headerRow.createCell(3).setCellValue("Overview Length");
        headerRow.createCell(4).setCellValue("Detail Length");
        headerRow.createCell(5).setCellValue("Status");
        headerRow.createCell(6).setCellValue("Notes");
    }

    private void populateTopicRow(Row row, Topic topic) {
        row.createCell(0).setCellValue(topic.getTopicLevel() != null ? topic.getTopicLevel() : "Intermediate");
        row.createCell(1).setCellValue(topic.getName());
        row.createCell(2).setCellValue(topic.getCategory());
        row.createCell(3).setCellValue(topic.getPriority());
        row.createCell(4).setCellValue(topic.getStatus().toString());
        row.createCell(5).setCellValue(topic.getDescription() != null ? topic.getDescription() : "");
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private double getCellValueAsNumber(Cell cell) {
        if (cell == null) return 0;

        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }

    private void createSampleTopicsFile() {
        log.info("Creating sample topics file at: {}", topicsExcelPath);
        List<Topic> sampleTopics = getDefaultTopics(10);
        addTopics(sampleTopics);
    }

    private void updateTopicInSheet(Sheet sheet, Topic topic, LocalDateTime processedTime) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row
            
            String rowTopicName = getCellValueAsString(row.getCell(1)); // Topic Name is in column 1
            if (rowTopicName != null && rowTopicName.trim().equals(topic.getName())) {
                // Update status to DONE
                Cell statusCell = row.getCell(4);
                if (statusCell == null) {
                    statusCell = row.createCell(4);
                }
                statusCell.setCellValue("DONE");
                
                // Add last processed timestamp (extend the row if needed)
                Cell lastProcessedCell = row.getCell(6);
                if (lastProcessedCell == null) {
                    lastProcessedCell = row.createCell(6);
                }
                lastProcessedCell.setCellValue(processedTime.format(DATE_FORMATTER));
                
                log.debug("Updated topic '{}' status to DONE in Excel sheet", topic.getName());
                break;
            }
        }
    }

    private List<Topic> getDefaultTopics(int limit) {
        List<Topic> defaultTopics = List.of(
                new Topic("Java Collections Framework", "Collections & Generics", "Intermediate", 5,
                         "Core framework for data structures like List, Set, and Map; understanding performance trade-offs is crucial for writing efficient code.")
        );

        return defaultTopics.stream().limit(limit).toList();
    }
}
