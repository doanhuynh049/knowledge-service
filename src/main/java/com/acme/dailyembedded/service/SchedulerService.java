package com.acme.dailyembedded.service;

import com.acme.dailyembedded.entity.*;
import com.acme.dailyembedded.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {

  private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

  private final UserRepository userRepository;
  private final PlanItemRepository planItemRepository;
  private final SettingRepository settingRepository;
  private final LessonService lessonService;
  private final MailService mailService;

  // Track processed deliveries to avoid duplicates
  private final ConcurrentHashMap<String, Long> processedDeliveries = new ConcurrentHashMap<>();

  public SchedulerService(
      UserRepository userRepository,
      PlanItemRepository planItemRepository,
      SettingRepository settingRepository,
      LessonService lessonService,
      MailService mailService) {
    this.userRepository = userRepository;
    this.planItemRepository = planItemRepository;
    this.settingRepository = settingRepository;
    this.lessonService = lessonService;
    this.mailService = mailService;
  }

  @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
  public void processScheduledEmails() {
    logger.debug("Starting scheduled email processing");

    List<User> users = userRepository.findAll();

    for (User user : users) {
      try {
        processUserScheduledEmails(user);
      } catch (Exception e) {
        logger.error("Failed to process scheduled emails for user {}", user.getEmail(), e);
      }
    }

    // Cleanup old processed deliveries (older than 25 hours)
    cleanupProcessedDeliveries();
  }

  private void processUserScheduledEmails(User user) {
    try {
      ZoneId userTimezone = ZoneId.of(user.getTimezone());
      ZonedDateTime userNow = ZonedDateTime.now(userTimezone);
      LocalTime currentTime = userNow.toLocalTime();
      LocalDate currentDate = userNow.toLocalDate();

      // Check if it's delivery time (within 10 minutes of delivery hour)
      LocalTime deliveryTime = LocalTime.of(user.getDeliveryHourLocal(), 0);
      if (!isWithinDeliveryWindow(currentTime, deliveryTime)) {
        return;
      }

      // Check if we've already processed this user today
      String deliveryKey = user.getId() + ":" + currentDate.toString();
      if (processedDeliveries.containsKey(deliveryKey)) {
        logger.debug("Already processed delivery for user {} on {}", user.getEmail(), currentDate);
        return;
      }

      // Get planned items for today
      List<PlanItem> plannedItems = planItemRepository.findPlannedItemsForUserAndDate(user.getId(), currentDate);
      if (plannedItems.isEmpty()) {
        logger.debug("No planned items for user {} on {}", user.getEmail(), currentDate);
        return;
      }

      // Get user settings
      Setting setting = settingRepository.findByUserId(user.getId())
          .orElse(new Setting(user.getId()));

      // Apply max deep dives per day limit
      List<PlanItem> itemsToProcess = plannedItems.stream()
          .limit(setting.getMaxDeepDivesPerDay())
          .toList();

      processUserEmails(user, currentDate, itemsToProcess, setting);

      // Mark items as sent
      itemsToProcess.forEach(item -> {
        item.setStatus(PlanItem.Status.SENT);
        planItemRepository.save(item);
      });

      // Track this delivery
      processedDeliveries.put(deliveryKey, System.currentTimeMillis());

      logger.info("Processed scheduled emails for user {} on {} - {} topics",
          user.getEmail(), currentDate, itemsToProcess.size());

    } catch (Exception e) {
      logger.error("Error processing scheduled emails for user {}", user.getEmail(), e);
    }
  }

  private void processUserEmails(User user, LocalDate date, List<PlanItem> planItems, Setting setting) {
    try {
      if (setting.getEmailMode() == Setting.EmailMode.DIGEST_AND_SPLIT) {
        processDigestAndSplitMode(user, date, planItems, setting);
      } else {
        processSingleMode(user, date, planItems, setting);
      }
    } catch (Exception e) {
      logger.error("Failed to process emails for user {} on {}", user.getEmail(), date, e);

      // Send fallback notification
      sendFallbackNotification(user, date, planItems.size());
    }
  }

  private void processDigestAndSplitMode(User user, LocalDate date, List<PlanItem> planItems, Setting setting) {
    // Generate and send overview digest
    try {
      Lesson overviewLesson = lessonService.generateLessonContent(user.getId(), date, planItems, setting);

      if (overviewLesson.getOverviewMd() != null) {
        String subject = mailService.generateDigestSubject(date.toString(), planItems.size());
        mailService.sendEmail(
            overviewLesson.getId(),
            EmailLog.Type.DIGEST,
            user.getEmail(),
            subject,
            overviewLesson.getOverviewMd()
        );
      }
    } catch (Exception e) {
      logger.error("Failed to send overview digest for user {}", user.getEmail(), e);
    }

    // Generate and send individual deep dives
    for (PlanItem planItem : planItems) {
      if ("deepdive".equals(planItem.getOutputType()) || "both".equals(planItem.getOutputType())) {
        try {
          Lesson deepDiveLesson = lessonService.generateSingleTopicLesson(user.getId(), date, planItem, setting);

          if (deepDiveLesson.getDeepDiveMd() != null) {
            String subject = mailService.generateDeepDiveSubject(planItem.getSeq(), planItem.getTopicTitle());
            mailService.sendEmail(
                deepDiveLesson.getId(),
                EmailLog.Type.DEEPDIVE,
                user.getEmail(),
                subject,
                deepDiveLesson.getDeepDiveMd()
            );
          }

          // Add delay between emails to avoid overwhelming SMTP
          Thread.sleep(2000);

        } catch (Exception e) {
          logger.error("Failed to send deep dive for topic '{}' to user {}",
              planItem.getTopicTitle(), user.getEmail(), e);
        }
      }
    }
  }

  private void processSingleMode(User user, LocalDate date, List<PlanItem> planItems, Setting setting) {
    try {
      // Generate combined lesson with overview + links to deep dives
      Lesson combinedLesson = lessonService.generateLessonContent(user.getId(), date, planItems, setting);

      StringBuilder combinedContent = new StringBuilder();

      // Add overview
      if (combinedLesson.getOverviewMd() != null) {
        combinedContent.append(combinedLesson.getOverviewMd());
        combinedContent.append("\n\n---\n\n");
      }

      // Add topic summaries with deep dive links
      combinedContent.append("## Individual Topic Deep Dives\n\n");
      for (PlanItem planItem : planItems) {
        combinedContent.append(String.format("### %d. %s\n", planItem.getSeq(), planItem.getTopicTitle()));
        if (planItem.getFocusPoints() != null) {
          combinedContent.append(String.format("**Focus:** %s\n\n", planItem.getFocusPoints()));
        }

        // Generate individual deep dive lesson for link reference
        try {
          lessonService.generateSingleTopicLesson(user.getId(), date, planItem, setting);
        } catch (Exception e) {
          logger.warn("Failed to generate deep dive for linking: {}", planItem.getTopicTitle(), e);
        }
      }

      String subject = mailService.generateDigestSubject(date.toString(), planItems.size());
      mailService.sendEmail(
          combinedLesson.getId(),
          EmailLog.Type.OVERVIEW,
          user.getEmail(),
          subject,
          combinedContent.toString()
      );

    } catch (Exception e) {
      logger.error("Failed to send single mode email for user {}", user.getEmail(), e);
      throw e;
    }
  }

  private void sendFallbackNotification(User user, LocalDate date, int topicCount) {
    try {
      String fallbackContent = String.format("""
          # Daily Learning Notification
          
          We encountered an issue generating your daily embedded systems content for %s.
          
          **Topics scheduled:** %d
          **Status:** Content generation failed
          
          Please check the system logs or contact support if this continues.
          
          ---
          *This is an automated fallback notification*
          """, date, topicCount);

      mailService.sendEmail(
          null,
          EmailLog.Type.OVERVIEW,
          user.getEmail(),
          "[Alert] Daily Learning Content Issue - " + date,
          fallbackContent
      );

    } catch (Exception e) {
      logger.error("Failed to send fallback notification to user {}", user.getEmail(), e);
    }
  }

  private boolean isWithinDeliveryWindow(LocalTime currentTime, LocalTime deliveryTime) {
    // Allow 10-minute window for delivery
    LocalTime windowStart = deliveryTime.minusMinutes(5);
    LocalTime windowEnd = deliveryTime.plusMinutes(5);

    return !currentTime.isBefore(windowStart) && !currentTime.isAfter(windowEnd);
  }

  private void cleanupProcessedDeliveries() {
    long cutoffTime = System.currentTimeMillis() - (25 * 60 * 60 * 1000); // 25 hours ago

    processedDeliveries.entrySet().removeIf(entry -> entry.getValue() < cutoffTime);

    if (processedDeliveries.size() > 1000) {
      logger.warn("Processed deliveries cache has {} entries, consider reducing retention",
          processedDeliveries.size());
    }
  }
}
