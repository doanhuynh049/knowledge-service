package com.knowledge.learning.service;

import com.knowledge.learning.model.LearningDay;
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
public class LearningExcelService {

    @Value("${app.learning-excel-path:6_month_learning_path.xlsx}")
    private String learningExcelPath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Column indices for the Excel file
    private static final int COL_DAY = 0;
    private static final int COL_PHASE = 1;
    private static final int COL_ALGORITHM_TASK = 2;
    private static final int COL_THEORY_TASK = 3;
    private static final int COL_CODING_TASK = 4;
    private static final int COL_REFLECTION_TASK = 5;
    private static final int COL_STATUS = 6;
    private static final int COL_NOTES = 7;
    private static final int COL_LAST_PROCESSED = 8;
    private static final int COL_COMPLETED_DATE = 9;
    private static final int COL_EMAIL_SUBJECT = 10;

    /**
     * Get next learning day to process
     */
    public LearningDay getNextLearningDay() {
        log.info("🔍 Searching for next learning day to process...");

        if (!new File(learningExcelPath).exists()) {
            log.warn("📁 Learning Excel file not found, creating with sample data: {}", learningExcelPath);
            createLearningFile();
        }

        try (FileInputStream fis = new FileInputStream(learningExcelPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("📋 Reading Excel sheet with {} rows", sheet.getLastRowNum());

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String status = getCellStringValue(row.getCell(COL_STATUS));
                if ("OPEN".equalsIgnoreCase(status)) {
                    LearningDay learningDay = mapRowToLearningDay(row);
                    log.info("✅ Found next learning day: Day {} - {} ({})",
                        learningDay.getDay(), learningDay.getPhase(), learningDay.getWeek());
                    return learningDay;
                }
            }

            log.info("🏁 No more OPEN learning days found");
            return null;

        } catch (IOException e) {
            log.error("❌ Error reading learning Excel file: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get all learning days for overview
     */
    public List<LearningDay> getAllLearningDays() {
        log.debug("📚 Retrieving all learning days...");
        List<LearningDay> learningDays = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(learningExcelPath);
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

        try (FileInputStream fis = new FileInputStream(learningExcelPath);
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
            try (FileOutputStream fos = new FileOutputStream(learningExcelPath)) {
                workbook.write(fos);
            }

            log.info("💾 Updated Day {} status to: {} (saved to Excel)", day, status);

        } catch (IOException e) {
            log.error("❌ Error updating learning day status: {}", e.getMessage(), e);
        }
    }

    /**
     * Create 6-month learning path Excel file with structured curriculum
     */
    private void createLearningFile() {
        log.info("📝 Creating 6-month learning path Excel file: {}", learningExcelPath);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("6 Month Learning Path");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Day", "Phase", "Algorithm Task", "Theory Task", 
                               "Coding Task", "Reflection Task", "Status", "Notes", 
                               "Last Processed", "Completed Date", "Email Subject"};

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Add comprehensive 180-day curriculum (6 months * 30 days)
            String[][] curriculumData = create6MonthCurriculum();

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
            for (int i = 0; i <= COL_EMAIL_SUBJECT; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save the file
            try (FileOutputStream fos = new FileOutputStream(learningExcelPath)) {
                workbook.write(fos);
            }

            log.info("✅ 6-month learning path Excel file created successfully with {} learning days", curriculumData.length);

        } catch (IOException e) {
            log.error("❌ Error creating learning Excel file: {}", e.getMessage(), e);
        }
    }

    /**
     * Create the 6-month curriculum data
     */
    private String[][] create6MonthCurriculum() {
        List<String[]> curriculum = new ArrayList<>();
        
        // Month 1: Programming Fundamentals (Days 1-30)
        addMonthCurriculum(curriculum, 1, "Programming Fundamentals", 
            "Variables, data types, control structures", 
            "Clean Code basics, naming conventions",
            "Simple programs, basic algorithms");
            
        // Month 2: Object-Oriented Programming (Days 31-60) 
        addMonthCurriculum(curriculum, 31, "Object-Oriented Programming",
            "Classes, inheritance, polymorphism",
            "SOLID principles, design patterns intro", 
            "Build small OOP projects");
            
        // Month 3: Data Structures & Algorithms (Days 61-90)
        addMonthCurriculum(curriculum, 61, "Data Structures & Algorithms",
            "Arrays, lists, stacks, queues, trees",
            "Algorithm complexity, Big O notation",
            "Implement data structures from scratch");
            
        // Month 4: System Design & Architecture (Days 91-120)
        addMonthCurriculum(curriculum, 91, "System Design & Architecture", 
            "System design basics, scalability",
            "Microservices, databases, caching",
            "Design simple distributed systems");
            
        // Month 5: Web Development & Frameworks (Days 121-150)
        addMonthCurriculum(curriculum, 121, "Web Development & Frameworks",
            "API design, REST principles", 
            "Web frameworks, security basics",
            "Build full-stack applications");
            
        // Month 6: Advanced Topics & Portfolio (Days 151-180)
        addMonthCurriculum(curriculum, 151, "Advanced Topics & Portfolio",
            "Advanced algorithms, optimization",
            "Performance tuning, testing strategies", 
            "Complete portfolio projects");
            
        return curriculum.toArray(new String[0][]);
    }
    
    /**
     * Add a month's worth of curriculum (30 days)
     */
    private void addMonthCurriculum(List<String[]> curriculum, int startDay, String phase, 
                                   String algorithmFocus, String theoryFocus, String codingFocus) {
        for (int i = 0; i < 30; i++) {
            int day = startDay + i;
            
            String algorithmTask = generateAlgorithmTask(day, algorithmFocus);
            String theoryTask = generateTheoryTask(day, theoryFocus);  
            String codingTask = generateCodingTask(day, codingFocus);
            String reflectionTask = "Write summary of today's learning + commit code to GitHub";
            String emailSubject = String.format("🚀 Day %d Learning Path - %s", day, phase);
            
            curriculum.add(new String[]{
                String.valueOf(day),
                phase,
                algorithmTask,
                theoryTask, 
                codingTask,
                reflectionTask,
                "OPEN",
                "",
                "",
                "",
                emailSubject
            });
        }
    }
    
    private String generateAlgorithmTask(int day, String focus) {
        int dayInMonth = ((day - 1) % 30) + 1;
        return String.format("Day %d: %s - Practice 2 problems", dayInMonth, focus);
    }
    
    private String generateTheoryTask(int day, String focus) {
        int dayInMonth = ((day - 1) % 30) + 1; 
        return String.format("Day %d: %s - Read 1 article/chapter", dayInMonth, focus);
    }
    
    private String generateCodingTask(int day, String focus) {
        int dayInMonth = ((day - 1) % 30) + 1;
        return String.format("Day %d: %s - 30-60min hands-on practice", dayInMonth, focus);
    }

    /**
     * Map Excel row to LearningDay object
     */
    private LearningDay mapRowToLearningDay(Row row) {
        LearningDay learningDay = new LearningDay();

        learningDay.setDay((int) row.getCell(COL_DAY).getNumericCellValue());
        learningDay.setWeek("Week " + (((int) row.getCell(COL_DAY).getNumericCellValue() - 1) / 7 + 1)); // Calculate week from day
        learningDay.setPhase(getCellStringValue(row.getCell(COL_PHASE)));
        learningDay.setAlgorithmTask(getCellStringValue(row.getCell(COL_ALGORITHM_TASK)));
        learningDay.setTheoryTask(getCellStringValue(row.getCell(COL_THEORY_TASK)));
        learningDay.setCodingTask(getCellStringValue(row.getCell(COL_CODING_TASK)));
        learningDay.setReflectionTask(getCellStringValue(row.getCell(COL_REFLECTION_TASK))); 
        learningDay.setStatus(getCellStringValue(row.getCell(COL_STATUS)));
        learningDay.setNotes(getCellStringValue(row.getCell(COL_NOTES)));
        learningDay.setLastProcessed(getCellStringValue(row.getCell(COL_LAST_PROCESSED)));
        learningDay.setCompletedDate(getCellStringValue(row.getCell(COL_COMPLETED_DATE)));
        learningDay.setEmailSubject(getCellStringValue(row.getCell(COL_EMAIL_SUBJECT)));

        return learningDay;
    }

    /**
     * Get learning progress statistics
     */
    public LearningProgress getLearningProgress() {
        log.debug("📊 Calculating learning progress...");

        List<LearningDay> allDays = getAllLearningDays();
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
