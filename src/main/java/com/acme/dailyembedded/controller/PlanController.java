package com.acme.dailyembedded.controller;

import com.acme.dailyembedded.dto.PlanItemCsvRow;
import com.acme.dailyembedded.entity.PlanItem;
import com.acme.dailyembedded.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

  private final PlanService planService;

  public PlanController(PlanService planService) {
    this.planService = planService;
  }

  @PostMapping("/items")
  public ResponseEntity<Map<String, Object>> bulkUpsertPlanItems(
      @RequestParam Long userId,
      @Valid @RequestBody List<PlanItemCsvRow> items) {

    List<PlanItem> savedItems = planService.bulkUpsertPlanItems(userId, items);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("savedItems", savedItems.size());
    response.put("message", "Plan items upserted successfully");

    return ResponseEntity.ok(response);
  }

  @PostMapping("/csv-upload")
  public ResponseEntity<Map<String, Object>> uploadCsv(
      @RequestParam Long userId,
      @RequestParam("file") MultipartFile file) {

    if (file.isEmpty()) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("error", "File is empty");
      return ResponseEntity.badRequest().body(response);
    }

    Map<String, Object> result = planService.importCsvFile(userId, file);
    result.put("success", true);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/daily")
  public ResponseEntity<Map<String, Object>> getDailyPlan(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam Long userId) {

    List<PlanItem> planItems = planService.getPlanItemsForDay(userId, date);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("date", date);
    response.put("userId", userId);
    response.put("items", planItems);
    response.put("totalItems", planItems.size());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/skip")
  public ResponseEntity<Map<String, Object>> skipPlanItem(@RequestBody Map<String, Object> request) {
    Long userId = Long.valueOf(request.get("userId").toString());
    LocalDate date = LocalDate.parse(request.get("date").toString());
    Integer seq = Integer.valueOf(request.get("seq").toString());

    PlanItem skippedItem = planService.skipPlanItem(userId, date, seq);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("skippedItem", Map.of(
        "id", skippedItem.getId(),
        "topicTitle", skippedItem.getTopicTitle(),
        "status", skippedItem.getStatus().toString()
    ));
    response.put("message", "Plan item marked as skipped");

    return ResponseEntity.ok(response);
  }
}
