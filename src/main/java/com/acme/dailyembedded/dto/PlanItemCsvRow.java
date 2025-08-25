package com.acme.dailyembedded.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public class PlanItemCsvRow {

  @NotNull(message = "Date is required")
  private LocalDate date;

  @NotNull(message = "Sequence is required")
  @Min(value = 1, message = "Sequence must be >= 1")
  private Integer seq;

  @NotBlank(message = "Topic title is required")
  private String topicTitle;

  private String focusPoints;
  private String learningGoal;
  private String difficulty = "intermediate";
  private String platforms;

  @Pattern(regexp = "en", message = "Language must be 'en'")
  private String language = "en";

  private String experienceLevel = "intermediate";

  @Pattern(regexp = "overview|deepdive|both", message = "Output type must be 'overview', 'deepdive', or 'both'")
  private String outputType = "both";

  private String authoritativeLinks;
  private String tags;
  private String notes;

  // Constructors
  public PlanItemCsvRow() {}

  // Getters and Setters
  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Integer getSeq() {
    return seq;
  }

  public void setSeq(Integer seq) {
    this.seq = seq;
  }

  public String getTopicTitle() {
    return topicTitle;
  }

  public void setTopicTitle(String topicTitle) {
    this.topicTitle = topicTitle;
  }

  public String getFocusPoints() {
    return focusPoints;
  }

  public void setFocusPoints(String focusPoints) {
    this.focusPoints = focusPoints;
  }

  public String getLearningGoal() {
    return learningGoal;
  }

  public void setLearningGoal(String learningGoal) {
    this.learningGoal = learningGoal;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
  }

  public String getPlatforms() {
    return platforms;
  }

  public void setPlatforms(String platforms) {
    this.platforms = platforms;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getExperienceLevel() {
    return experienceLevel;
  }

  public void setExperienceLevel(String experienceLevel) {
    this.experienceLevel = experienceLevel;
  }

  public String getOutputType() {
    return outputType;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  public String getAuthoritativeLinks() {
    return authoritativeLinks;
  }

  public void setAuthoritativeLinks(String authoritativeLinks) {
    this.authoritativeLinks = authoritativeLinks;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
