package com.acme.dailyembedded.service;

import com.acme.dailyembedded.entity.Lesson;
import com.acme.dailyembedded.entity.PlanItem;
import com.acme.dailyembedded.entity.Setting;
import com.acme.dailyembedded.repository.LessonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LessonService {

  private static final Logger logger = LoggerFactory.getLogger(LessonService.class);

  private final LessonRepository lessonRepository;
  private final LlmService llmService;
  private final ObjectMapper objectMapper;

  private String overviewPromptTemplate;
  private String deepDivePromptTemplate;

  public LessonService(LessonRepository lessonRepository, LlmService llmService) {
    this.lessonRepository = lessonRepository;
    this.llmService = llmService;
    this.objectMapper = new ObjectMapper();
    loadPromptTemplates();
  }

  public Lesson generateLessonContent(Long userId, LocalDate date, List<PlanItem> planItems, Setting setting) {
    if (planItems.isEmpty()) {
      throw new RuntimeException("No plan items provided for lesson generation");
    }

    try {
      // Create lesson for the first item's sequence (overview lesson)
      PlanItem firstItem = planItems.get(0);
      Lesson lesson = lessonRepository.findByUserIdAndDateAndSeq(userId, date, firstItem.getSeq())
          .orElse(new Lesson(userId, date, firstItem.getSeq()));

      // Store topic snapshot
      lesson.setTopicSnapshotJson(objectMapper.writeValueAsString(planItems));

      // Generate overview for all topics
      String overviewContent = generateOverview(planItems, setting);
      lesson.setOverviewMd(overviewContent);

      // Generate deep dive for single topic (if applicable)
      if (planItems.size() == 1 && shouldGenerateDeepDive(planItems.get(0))) {
        String deepDiveContent = generateDeepDive(planItems.get(0), setting);
        lesson.setDeepDiveMd(deepDiveContent);
      }

      return lessonRepository.save(lesson);

    } catch (Exception e) {
      logger.error("Failed to generate lesson content for user {} on date {}", userId, date, e);
      throw new RuntimeException("Lesson content generation failed", e);
    }
  }

  public Lesson generateSingleTopicLesson(Long userId, LocalDate date, PlanItem planItem, Setting setting) {
    try {
      Lesson lesson = lessonRepository.findByUserIdAndDateAndSeq(userId, date, planItem.getSeq())
          .orElse(new Lesson(userId, date, planItem.getSeq()));

      // Store topic snapshot
      lesson.setTopicSnapshotJson(objectMapper.writeValueAsString(List.of(planItem)));

      // Generate overview if needed
      if (shouldGenerateOverview(planItem)) {
        String overviewContent = generateOverview(List.of(planItem), setting);
        lesson.setOverviewMd(overviewContent);
      }

      // Generate deep dive if needed
      if (shouldGenerateDeepDive(planItem)) {
        String deepDiveContent = generateDeepDive(planItem, setting);
        lesson.setDeepDiveMd(deepDiveContent);
      }

      return lessonRepository.save(lesson);

    } catch (Exception e) {
      logger.error("Failed to generate single topic lesson for user {} on date {}", userId, date, e);
      throw new RuntimeException("Single topic lesson generation failed", e);
    }
  }

  private String generateOverview(List<PlanItem> planItems, Setting setting) {
    try {
      String userPrompt = buildOverviewPrompt(planItems);

      return llmService.generateContent(
          overviewPromptTemplate,
          userPrompt,
          setting.getModel(),
          setting.getTemperature(),
          setting.getMaxTokens()
      );

    } catch (Exception e) {
      logger.error("Failed to generate overview content", e);
      return generateFallbackOverview(planItems);
    }
  }

  private String generateDeepDive(PlanItem planItem, Setting setting) {
    try {
      String userPrompt = buildDeepDivePrompt(planItem);

      return llmService.generateContent(
          deepDivePromptTemplate,
          userPrompt,
          setting.getModel(),
          setting.getTemperature(),
          setting.getMaxTokens()
      );

    } catch (Exception e) {
      logger.error("Failed to generate deep dive content for topic: {}", planItem.getTopicTitle(), e);
      return generateFallbackDeepDive(planItem);
    }
  }

  private String buildOverviewPrompt(List<PlanItem> planItems) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Generate a daily overview for the following embedded systems topics:\n\n");

    for (int i = 0; i < planItems.size(); i++) {
      PlanItem item = planItems.get(i);
      prompt.append(String.format("Topic %d: %s\n", i + 1, item.getTopicTitle()));
      if (item.getFocusPoints() != null) {
        prompt.append(String.format("Focus Points: %s\n", item.getFocusPoints()));
      }
      if (item.getLearningGoal() != null) {
        prompt.append(String.format("Learning Goal: %s\n", item.getLearningGoal()));
      }
      prompt.append(String.format("Difficulty: %s\n", item.getDifficulty()));
      prompt.append(String.format("Experience Level: %s\n", item.getExperienceLevel()));
      prompt.append("\n");
    }

    return prompt.toString();
  }

  private String buildDeepDivePrompt(PlanItem planItem) {
    return String.format("""
        Generate a deep-dive lesson for this embedded systems topic:
        
        Topic: %s
        Focus Points: %s
        Learning Goal: %s
        Difficulty: %s
        Platforms: %s
        Experience Level: %s
        Authoritative Links: %s
        Tags: %s
        Notes: %s
        """,
        planItem.getTopicTitle(),
        planItem.getFocusPoints() != null ? planItem.getFocusPoints() : "General exploration",
        planItem.getLearningGoal() != null ? planItem.getLearningGoal() : "Understand core concepts",
        planItem.getDifficulty(),
        planItem.getPlatforms() != null ? planItem.getPlatforms() : "General embedded platforms",
        planItem.getExperienceLevel(),
        planItem.getAuthoritativeLinks() != null ? planItem.getAuthoritativeLinks() : "None specified",
        planItem.getTags() != null ? planItem.getTags() : "embedded-systems",
        planItem.getNotes() != null ? planItem.getNotes() : "None"
    );
  }

  private boolean shouldGenerateOverview(PlanItem planItem) {
    return "overview".equals(planItem.getOutputType()) || "both".equals(planItem.getOutputType());
  }

  private boolean shouldGenerateDeepDive(PlanItem planItem) {
    return "deepdive".equals(planItem.getOutputType()) || "both".equals(planItem.getOutputType());
  }

  private String generateFallbackOverview(List<PlanItem> planItems) {
    StringBuilder fallback = new StringBuilder();
    fallback.append("# Daily Overview\n\n");
    fallback.append("Today's embedded systems learning topics:\n\n");

    for (int i = 0; i < planItems.size(); i++) {
      PlanItem item = planItems.get(i);
      fallback.append(String.format("## %d. %s\n", i + 1, item.getTopicTitle()));
      if (item.getFocusPoints() != null) {
        fallback.append(String.format("**Focus:** %s\n\n", item.getFocusPoints()));
      }
      if (item.getLearningGoal() != null) {
        fallback.append(String.format("**Goal:** %s\n\n", item.getLearningGoal()));
      }
    }

    fallback.append("\n*Note: LLM content generation failed - this is a fallback overview.*");
    return fallback.toString();
  }

  private String generateFallbackDeepDive(PlanItem planItem) {
    return String.format("""
        # %s
        
        ## Overview
        This is a deep-dive lesson on %s.
        
        **Difficulty:** %s  
        **Experience Level:** %s
        
        ## Focus Points
        %s
        
        ## Learning Goal
        %s
        
        ## Notes
        %s
        
        ---
        *Note: LLM content generation failed - this is a fallback deep-dive lesson.*
        """,
        planItem.getTopicTitle(),
        planItem.getTopicTitle(),
        planItem.getDifficulty(),
        planItem.getExperienceLevel(),
        planItem.getFocusPoints() != null ? planItem.getFocusPoints() : "General exploration of the topic",
        planItem.getLearningGoal() != null ? planItem.getLearningGoal() : "Understand core concepts and practical applications",
        planItem.getNotes() != null ? planItem.getNotes() : "No additional notes provided"
    );
  }

  private void loadPromptTemplates() {
    try {
      ClassPathResource overviewResource = new ClassPathResource("prompts/overview.txt");
      ClassPathResource deepDiveResource = new ClassPathResource("prompts/deepdive.txt");

      overviewPromptTemplate = overviewResource.getContentAsString(StandardCharsets.UTF_8);
      deepDivePromptTemplate = deepDiveResource.getContentAsString(StandardCharsets.UTF_8);

      logger.info("Loaded LLM prompt templates successfully");

    } catch (IOException e) {
      logger.warn("Failed to load prompt templates, using defaults", e);

      overviewPromptTemplate = """
          You are an expert embedded systems engineer creating daily learning overviews.
          Create a concise overview covering all provided topics with:
          - 1-2 key bullets per topic
          - 1 gotcha and 1 best practice per topic  
          - 3 authoritative links total
          - 3-question micro-quiz with answers at the end
          Format in clean Markdown.
          """;

      deepDivePromptTemplate = """
          You are an expert embedded systems engineer creating detailed technical lessons.
          Create a comprehensive deep-dive including:
          - Mental model explanation
          - ASCII timing/architecture diagrams where relevant
          - Step-by-step practical lab (C or Rust code examples)
          - Debugging checklist
          - Performance and power considerations
          - Vendor-specific application notes
          - Hardware risk caution blocks
          Format in clean Markdown with code blocks.
          """;
    }
  }
}
