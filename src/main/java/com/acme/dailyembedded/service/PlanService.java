package com.acme.dailyembedded.service;

import com.acme.dailyembedded.dto.PlanItemCsvRow;
import com.acme.dailyembedded.entity.PlanItem;
import com.acme.dailyembedded.repository.PlanItemRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanService {

  private static final Logger logger = LoggerFactory.getLogger(PlanService.class);

  private final PlanItemRepository planItemRepository;

  public PlanService(PlanItemRepository planItemRepository) {
    this.planItemRepository = planItemRepository;
  }

  public List<PlanItem> getPlanItemsForDay(Long userId, LocalDate date) {
    return planItemRepository.findByUserIdAndDateOrderBySeq(userId, date);
  }

  public List<PlanItem> getPlannedItemsForDay(Long userId, LocalDate date) {
    return planItemRepository.findPlannedItemsForUserAndDate(userId, date);
  }

  public PlanItem skipPlanItem(Long userId, LocalDate date, Integer seq) {
    PlanItem planItem = planItemRepository.findByUserIdAndDateAndSeq(userId, date, seq)
        .orElseThrow(() -> new RuntimeException("Plan item not found"));

    planItem.setStatus(PlanItem.Status.SKIPPED);
    return planItemRepository.save(planItem);
  }

  public List<PlanItem> bulkUpsertPlanItems(Long userId, List<PlanItemCsvRow> items) {
    List<PlanItem> savedItems = new ArrayList<>();

    for (PlanItemCsvRow row : items) {
      PlanItem planItem = planItemRepository
          .findByUserIdAndDateAndSeq(userId, row.getDate(), row.getSeq())
          .orElse(new PlanItem(userId, row.getDate(), row.getSeq(), row.getTopicTitle()));

      // Update all fields
      planItem.setTopicTitle(row.getTopicTitle());
      planItem.setFocusPoints(row.getFocusPoints());
      planItem.setLearningGoal(row.getLearningGoal());
      planItem.setDifficulty(row.getDifficulty());
      planItem.setPlatforms(row.getPlatforms());
      planItem.setLanguage(row.getLanguage());
      planItem.setExperienceLevel(row.getExperienceLevel());
      planItem.setOutputType(row.getOutputType());
      planItem.setAuthoritativeLinks(row.getAuthoritativeLinks());
      planItem.setTags(row.getTags());
      planItem.setNotes(row.getNotes());

      savedItems.add(planItemRepository.save(planItem));
    }

    logger.info("Bulk upserted {} plan items for user {}", savedItems.size(), userId);
    return savedItems;
  }

  public Map<String, Object> importCsvFile(Long userId, MultipartFile file) {
    Map<String, Object> result = new HashMap<>();
    List<String> errors = new ArrayList<>();
    List<PlanItemCsvRow> validRows = new ArrayList<>();
    int totalRows = 0;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

      CSVFormat format = CSVFormat.DEFAULT.builder()
          .setHeader("date", "seq", "topic_title", "focus_points", "learning_goal",
                    "difficulty", "platforms", "language", "experience_level",
                    "output_type", "authoritative_links", "tags", "notes")
          .setSkipHeaderRecord(true)
          .build();

      CSVParser parser = format.parse(reader);

      for (CSVRecord record : parser) {
        totalRows++;
        try {
          PlanItemCsvRow row = parseCsvRecord(record);
          String validationError = validateCsvRow(row, userId);

          if (validationError != null) {
            errors.add("Row " + totalRows + ": " + validationError);
          } else {
            validRows.add(row);
          }

        } catch (Exception e) {
          errors.add("Row " + totalRows + ": " + e.getMessage());
        }
      }

    } catch (Exception e) {
      logger.error("Error processing CSV file", e);
      errors.add("File processing error: " + e.getMessage());
    }

    // Save valid rows
    List<PlanItem> savedItems = new ArrayList<>();
    if (!validRows.isEmpty()) {
      savedItems = bulkUpsertPlanItems(userId, validRows);
    }

    result.put("totalRows", totalRows);
    result.put("validRows", validRows.size());
    result.put("savedItems", savedItems.size());
    result.put("errors", errors);

    return result;
  }

  private PlanItemCsvRow parseCsvRecord(CSVRecord record) {
    PlanItemCsvRow row = new PlanItemCsvRow();

    try {
      row.setDate(LocalDate.parse(record.get("date")));
    } catch (DateTimeParseException e) {
      throw new RuntimeException("Invalid date format: " + record.get("date"));
    }

    try {
      row.setSeq(Integer.parseInt(record.get("seq")));
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid sequence number: " + record.get("seq"));
    }

    row.setTopicTitle(record.get("topic_title"));
    row.setFocusPoints(record.get("focus_points"));
    row.setLearningGoal(record.get("learning_goal"));
    row.setDifficulty(record.get("difficulty"));
    row.setPlatforms(record.get("platforms"));
    row.setLanguage(record.get("language"));
    row.setExperienceLevel(record.get("experience_level"));
    row.setOutputType(record.get("output_type"));
    row.setAuthoritativeLinks(record.get("authoritative_links"));
    row.setTags(record.get("tags"));
    row.setNotes(record.get("notes"));

    return row;
  }

  private String validateCsvRow(PlanItemCsvRow row, Long userId) {
    if (row.getSeq() < 1) {
      return "Sequence must be >= 1";
    }

    if (row.getTopicTitle() == null || row.getTopicTitle().trim().isEmpty()) {
      return "Topic title is required";
    }

    if (row.getLanguage() != null && !row.getLanguage().equals("en")) {
      return "Language must be 'en'";
    }

    if (row.getOutputType() != null &&
        !List.of("overview", "deepdive", "both").contains(row.getOutputType())) {
      return "Output type must be 'overview', 'deepdive', or 'both'";
    }

    return null; // No validation errors
  }
}
