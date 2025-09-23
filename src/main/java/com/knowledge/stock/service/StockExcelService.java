package com.knowledge.stock.service;

import com.knowledge.stock.model.StockLearningDay;
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
public class StockExcelService {

    @Value("${app.stock-excel-path:enhanced_stock_learning.xlsx}")
    private String stockExcelPath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Enhanced column indices for structured learning
    private static final int COL_DAY = 0;
    private static final int COL_WEEK = 1;
    private static final int COL_PHASE = 2;
    private static final int COL_TOPIC = 3;
    private static final int COL_LEARNING_GOAL = 4;
    private static final int COL_EMAIL_SUBJECT = 5;
    private static final int COL_PRACTICE_TASK = 6;
    private static final int COL_STATUS = 7;
    private static final int COL_NOTES = 8;
    private static final int COL_LAST_PROCESSED = 9;
    private static final int COL_COMPLETED_DATE = 10;

    /**
     * Get next learning day to process
     */
    public StockLearningDay getNextLearningDay() {
        log.info("🔍 Searching for next learning day to process...");

        if (!new File(stockExcelPath).exists()) {
            log.warn("📁 Stock learning Excel file not found, creating with sample data: {}", stockExcelPath);
            createEnhancedStockLearningFile();
        }

        try (FileInputStream fis = new FileInputStream(stockExcelPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📋 Reading Excel sheet with {} rows", sheet.getLastRowNum());

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String status = getCellStringValue(row.getCell(COL_STATUS));
                if ("OPEN".equalsIgnoreCase(status)) {
                    StockLearningDay learningDay = mapRowToLearningDay(row);
                    log.info("✅ Found next learning day: Day {} - {} ({})",
                        learningDay.getDay(), learningDay.getTopic(), learningDay.getPhase());
                    return learningDay;
                }
            }

            log.info("🏁 No more OPEN learning days found");
            return null;

        } catch (IOException e) {
            log.error("❌ Error reading stock learning Excel file: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get all learning days for overview
     */
    public List<StockLearningDay> getAllLearningDays() {
        log.debug("📚 Retrieving all learning days...");
        List<StockLearningDay> learningDays = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(stockExcelPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                learningDays.add(mapRowToLearningDay(row));
            }

            log.info("📊 Retrieved {} total learning days", learningDays.size());

        } catch (IOException e) {
            log.error("❌ Error reading all learning days: {}", e.getMessage(), e);
        }

        return learningDays;
    }

    /**
     * Mark learning day as completed
     */
    public void markLearningDayCompleted(int day, String notes) {
        log.info("✅ Marking Day {} as COMPLETED", day);
        updateLearningDayStatusInternal(day, "COMPLETED", notes, LocalDateTime.now().format(DATE_FORMATTER));
    }

    /**
     * Mark learning day as having an error
     */
    public void markLearningDayError(int day, String errorMessage) {
        log.error("❌ Marking Day {} as ERROR: {}", day, errorMessage);
        updateLearningDayStatusInternal(day, "ERROR", errorMessage, "");
    }

    /**
     * Update learning day status in Excel (public method for external calls)
     */
    public void updateLearningDayStatus(int day, String status, String notes, String completedDate) {
        log.debug("🔄 Public update requested for Day {} status to: {}", day, status);
        updateLearningDayStatusInternal(day, status, notes, completedDate);
    }

    /**
     * Update learning day status in Excel
     */
    private void updateLearningDayStatusInternal(int day, String status, String notes, String completedDate) {
        log.debug("🔄 Updating Day {} status to: {}", day, status);

        try (FileInputStream fis = new FileInputStream(stockExcelPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                Cell dayCell = row.getCell(COL_DAY);
                int rowDay = (int) dayCell.getNumericCellValue();

                if (rowDay == day) {
                    // Update status
                    Cell statusCell = row.getCell(COL_STATUS);
                    if (statusCell == null) {
                        statusCell = row.createCell(COL_STATUS);
                    }
                    statusCell.setCellValue(status);

                    // Update notes
                    Cell notesCell = row.getCell(COL_NOTES);
                    if (notesCell == null) {
                        notesCell = row.createCell(COL_NOTES);
                    }
                    notesCell.setCellValue(notes);

                    // Update last processed time
                    Cell lastProcessedCell = row.getCell(COL_LAST_PROCESSED);
                    if (lastProcessedCell == null) {
                        lastProcessedCell = row.createCell(COL_LAST_PROCESSED);
                    }
                    lastProcessedCell.setCellValue(LocalDateTime.now().format(DATE_FORMATTER));

                    // Update completed date if provided
                    if (!completedDate.isEmpty()) {
                        Cell completedCell = row.getCell(COL_COMPLETED_DATE);
                        if (completedCell == null) {
                            completedCell = row.createCell(COL_COMPLETED_DATE);
                        }
                        completedCell.setCellValue(completedDate);
                    }

                    break;
                }
            }

            // Save the file
            try (FileOutputStream fos = new FileOutputStream(stockExcelPath)) {
                workbook.write(fos);
            }

            log.info("💾 Updated Day {} status to: {} (saved to Excel)", day, status);

        } catch (IOException e) {
            log.error("❌ Error updating learning day status: {}", e.getMessage(), e);
        }
    }

    /**
     * Create enhanced stock learning Excel file with structured curriculum
     */
    private void createEnhancedStockLearningFile() {
        log.info("📝 Creating enhanced stock learning Excel file: {}", stockExcelPath);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Stock Learning Curriculum");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Day", "Week", "Phase", "Topic", "Learning Goal", "Email Subject",
                               "Practice Task", "Status", "Notes", "Last Processed", "Completed Date"};

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Add comprehensive 20-day curriculum
            String[][] curriculumData = {
                // Week 1 - Foundations
                {"1", "Week 1", "Foundations", "Stock basics: What is a stock?",
                 "Understand and apply: Stock basics: What is a stock?",
                 "📈 Daily Stock Knowledge – Stock basics: What is a stock?",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"2", "Week 1", "Foundations", "Exchanges & indices (VN-Index, S&P500, Nasdaq)",
                 "Understand and apply: Exchanges & indices (VN-Index, S&P500, Nasdaq)",
                 "📈 Daily Stock Knowledge – Exchanges & indices (VN-Index, S&P500, Nasdaq)",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"3", "Week 1", "Foundations", "Types of stocks: growth, value, dividend",
                 "Understand and apply: Types of stocks: growth, value, dividend",
                 "📈 Daily Stock Knowledge – Types of stocks: growth, value, dividend",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"4", "Week 1", "Foundations", "How stock trading works (brokers, clearing)",
                 "Understand and apply: How stock trading works (brokers, clearing)",
                 "📈 Daily Stock Knowledge – How stock trading works (brokers, clearing)",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"5", "Week 1", "Foundations", "Order types: market, limit, stop-loss",
                 "Understand and apply: Order types: market, limit, stop-loss",
                 "📈 Daily Stock Knowledge – Order types: market, limit, stop-loss",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                // Week 2 - Analysis Fundamentals
                {"6", "Week 2", "Analysis", "Reading financial statements basics",
                 "Understand and apply: Reading financial statements basics",
                 "📈 Daily Stock Knowledge – Reading financial statements basics",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"7", "Week 2", "Analysis", "Key financial ratios (P/E, ROE, Debt-to-Equity)",
                 "Understand and apply: Key financial ratios (P/E, ROE, Debt-to-Equity)",
                 "📈 Daily Stock Knowledge – Key financial ratios (P/E, ROE, Debt-to-Equity)",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"8", "Week 2", "Analysis", "Revenue and profit analysis",
                 "Understand and apply: Revenue and profit analysis",
                 "📈 Daily Stock Knowledge – Revenue and profit analysis",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"9", "Week 2", "Analysis", "Cash flow statement analysis",
                 "Understand and apply: Cash flow statement analysis",
                 "📈 Daily Stock Knowledge – Cash flow statement analysis",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"10", "Week 2", "Analysis", "Industry and competitor analysis",
                 "Understand and apply: Industry and competitor analysis",
                 "📈 Daily Stock Knowledge – Industry and competitor analysis",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                // Week 3 - Technical Analysis
                {"11", "Week 3", "Technical", "Chart reading basics: candlesticks, trends",
                 "Understand and apply: Chart reading basics: candlesticks, trends",
                 "📈 Daily Stock Knowledge – Chart reading basics: candlesticks, trends",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"12", "Week 3", "Technical", "Support and resistance levels",
                 "Understand and apply: Support and resistance levels",
                 "📈 Daily Stock Knowledge – Support and resistance levels",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"13", "Week 3", "Technical", "Moving averages and volume analysis",
                 "Understand and apply: Moving averages and volume analysis",
                 "📈 Daily Stock Knowledge – Moving averages and volume analysis",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"14", "Week 3", "Technical", "Technical indicators: RSI, MACD, Bollinger Bands",
                 "Understand and apply: Technical indicators: RSI, MACD, Bollinger Bands",
                 "📈 Daily Stock Knowledge – Technical indicators: RSI, MACD, Bollinger Bands",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"15", "Week 3", "Technical", "Chart patterns: triangles, flags, head & shoulders",
                 "Understand and apply: Chart patterns: triangles, flags, head & shoulders",
                 "📈 Daily Stock Knowledge – Chart patterns: triangles, flags, head & shoulders",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                // Week 4 - Risk & Strategy
                {"16", "Week 4", "Strategy", "Portfolio diversification principles",
                 "Understand and apply: Portfolio diversification principles",
                 "📈 Daily Stock Knowledge – Portfolio diversification principles",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"17", "Week 4", "Strategy", "Risk management and position sizing",
                 "Understand and apply: Risk management and position sizing",
                 "📈 Daily Stock Knowledge – Risk management and position sizing",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"18", "Week 4", "Strategy", "Value investing vs growth investing",
                 "Understand and apply: Value investing vs growth investing",
                 "📈 Daily Stock Knowledge – Value investing vs growth investing",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"19", "Week 4", "Strategy", "Dollar-cost averaging and timing strategies",
                 "Understand and apply: Dollar-cost averaging and timing strategies",
                 "📈 Daily Stock Knowledge – Dollar-cost averaging and timing strategies",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""},

                {"20", "Week 4", "Strategy", "Market psychology and behavioral finance",
                 "Understand and apply: Market psychology and behavioral finance",
                 "📈 Daily Stock Knowledge – Market psychology and behavioral finance",
                 "Study 20m + Apply 15m. Analyze 1 VN and 1 US ticker using today's topic; write 3 bullet insights and a decision.",
                 "OPEN", "", "", ""}
            };

            for (int i = 0; i < curriculumData.length; i++) {
                Row row = sheet.createRow(i + 1);
                String[] data = curriculumData[i];

                // Day (numeric)
                row.createCell(0).setCellValue(Integer.parseInt(data[0]));

                // All other fields (text)
                for (int j = 1; j < data.length; j++) {
                    row.createCell(j).setCellValue(data[j]);
                }
            }

            // Auto-size columns
            for (int i = 0; i <= COL_COMPLETED_DATE; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save the file
            try (FileOutputStream fos = new FileOutputStream(stockExcelPath)) {
                workbook.write(fos);
            }

            log.info("✅ Enhanced stock learning Excel file created successfully with {} learning days", curriculumData.length);

        } catch (IOException e) {
            log.error("❌ Error creating enhanced stock Excel file: {}", e.getMessage(), e);
        }
    }

    /**
     * Map Excel row to StockLearningDay object
     */
    private StockLearningDay mapRowToLearningDay(Row row) {
        StockLearningDay learningDay = new StockLearningDay();

        learningDay.setDay((int) row.getCell(COL_DAY).getNumericCellValue());
        learningDay.setWeek(getCellStringValue(row.getCell(COL_WEEK)));
        learningDay.setPhase(getCellStringValue(row.getCell(COL_PHASE)));
        learningDay.setTopic(getCellStringValue(row.getCell(COL_TOPIC)));
        learningDay.setLearningGoal(getCellStringValue(row.getCell(COL_LEARNING_GOAL)));
        learningDay.setEmailSubject(getCellStringValue(row.getCell(COL_EMAIL_SUBJECT)));
        learningDay.setPracticeTask(getCellStringValue(row.getCell(COL_PRACTICE_TASK)));
        learningDay.setStatus(getCellStringValue(row.getCell(COL_STATUS)));
        learningDay.setNotes(getCellStringValue(row.getCell(COL_NOTES)));
        learningDay.setLastProcessed(getCellStringValue(row.getCell(COL_LAST_PROCESSED)));
        learningDay.setCompletedDate(getCellStringValue(row.getCell(COL_COMPLETED_DATE)));

        return learningDay;
    }

    /**
     * Get learning progress statistics
     */
    public LearningProgress getLearningProgress() {
        log.debug("📊 Calculating learning progress...");

        List<StockLearningDay> allDays = getAllLearningDays();
        long completedDays = allDays.stream().filter(day -> "COMPLETED".equals(day.getStatus())).count();
        long errorDays = allDays.stream().filter(day -> "ERROR".equals(day.getStatus())).count();
        long openDays = allDays.stream().filter(day -> "OPEN".equals(day.getStatus())).count();

        double completionRate = allDays.size() > 0 ? (double) completedDays / allDays.size() * 100 : 0;

        LearningProgress progress = new LearningProgress(
            allDays.size(), (int) completedDays, (int) errorDays, (int) openDays, completionRate
        );

        log.info("📈 Learning Progress: {}% complete ({}/{} days)",
            String.format("%.1f", completionRate), completedDays, allDays.size());

        return progress;
    }

    /**
     * Get string value from cell safely
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    // Inner class for learning progress
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class LearningProgress {
        private int totalDays;
        private int completedDays;
        private int errorDays;
        private int openDays;
        private double completionRate;
    }
}
