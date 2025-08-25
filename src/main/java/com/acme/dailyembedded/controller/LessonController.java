package com.acme.dailyembedded.controller;

import com.acme.dailyembedded.entity.EmailLog;
import com.acme.dailyembedded.entity.Lesson;
import com.acme.dailyembedded.entity.PlanItem;
import com.acme.dailyembedded.entity.Setting;
import com.acme.dailyembedded.repository.LessonRepository;
import com.acme.dailyembedded.repository.PlanItemRepository;
import com.acme.dailyembedded.repository.SettingRepository;
import com.acme.dailyembedded.repository.UserRepository;
import com.acme.dailyembedded.service.LessonService;
import com.acme.dailyembedded.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/lessons")
public class LessonController {

  private final LessonRepository lessonRepository;
  private final PlanItemRepository planItemRepository;
  private final SettingRepository settingRepository;
  private final UserRepository userRepository;
  private final LessonService lessonService;
  private final MailService mailService;

  public LessonController(
      LessonRepository lessonRepository,
      PlanItemRepository planItemRepository,
      SettingRepository settingRepository,
      UserRepository userRepository,
      LessonService lessonService,
      MailService mailService) {
    this.lessonRepository = lessonRepository;
    this.planItemRepository = planItemRepository;
    this.settingRepository = settingRepository;
    this.userRepository = userRepository;
    this.lessonService = lessonService;
    this.mailService = mailService;
  }

  @PostMapping("/resend")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> resendLesson(@RequestBody ResendLessonRequest request) {
    try {
      Long userId = request.getUserId();
      LocalDate date = request.getDate();
      String type = request.getType();

      var user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));

      var setting = settingRepository.findByUserId(userId)
          .orElse(new Setting(userId));

      if ("digest".equalsIgnoreCase(type)) {
        // Resend digest (overview of all topics for the day)
        var planItems = planItemRepository.findByUserIdAndDateOrderBySeq(userId, date);
        if (!planItems.isEmpty()) {
          var lesson = lessonService.generateLessonContent(userId, date, planItems, setting);

          String subject = mailService.generateDigestSubject(date.toString(), planItems.size());
          mailService.sendEmail(
              lesson.getId(),
              EmailLog.Type.DIGEST,
              user.getEmail(),
              subject,
              lesson.getOverviewMd()
          );
        }
      } else if (request.getSeq() != null) {
        // Resend specific deep dive
        var planItem = planItemRepository.findByUserIdAndDateAndSeq(userId, date, request.getSeq())
            .orElseThrow(() -> new RuntimeException("Plan item not found"));

        var lesson = lessonService.generateSingleTopicLesson(userId, date, planItem, setting);

        String subject = mailService.generateDeepDiveSubject(planItem.getSeq(), planItem.getTopicTitle());
        mailService.sendEmail(
            lesson.getId(),
            EmailLog.Type.DEEPDIVE,
            user.getEmail(),
            subject,
            lesson.getDeepDiveMd()
        );
      }

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Lesson resent successfully");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("error", e.getMessage());

      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/{id}")
  public String viewLesson(@PathVariable Long id, Model model) {
    Optional<Lesson> lessonOpt = lessonRepository.findById(id);

    if (lessonOpt.isEmpty()) {
      model.addAttribute("error", "Lesson not found");
      return "lesson-not-found";
    }

    Lesson lesson = lessonOpt.get();
    model.addAttribute("lesson", lesson);
    model.addAttribute("hasOverview", lesson.getOverviewMd() != null);
    model.addAttribute("hasDeepDive", lesson.getDeepDiveMd() != null);

    return "lesson-view";
  }

  @GetMapping("/{id}/api")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getLessonApi(@PathVariable Long id) {
    return lessonRepository.findById(id)
        .map(lesson -> {
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("lesson", Map.of(
              "id", lesson.getId(),
              "userId", lesson.getUserId(),
              "date", lesson.getDate(),
              "seq", lesson.getSeq(),
              "overviewMd", lesson.getOverviewMd() != null ? lesson.getOverviewMd() : "",
              "deepDiveMd", lesson.getDeepDiveMd() != null ? lesson.getDeepDiveMd() : "",
              "createdAt", lesson.getCreatedAt()
          ));
          return ResponseEntity.ok(response);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  public static class ResendLessonRequest {
    private Long userId;
    private LocalDate date;
    private Integer seq;
    private String type;

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
  }
}
