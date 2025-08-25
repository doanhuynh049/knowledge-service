package com.acme.dailyembedded.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
    name = "plan_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date", "seq"}))
public class PlanItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private Integer seq;

  @Column(name = "topic_title", nullable = false)
  private String topicTitle;

  @Column(name = "focus_points", columnDefinition = "TEXT")
  private String focusPoints;

  @Column(name = "learning_goal", columnDefinition = "TEXT")
  private String learningGoal;

  private String difficulty = "intermediate";

  private String platforms;

  private String language = "en";

  @Column(name = "experience_level")
  private String experienceLevel = "intermediate";

  @Column(name = "output_type")
  private String outputType = "both";

  @Column(name = "authoritative_links", columnDefinition = "TEXT")
  private String authoritativeLinks;

  private String tags;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Enumerated(EnumType.STRING)
  private Status status = Status.PLANNED;

  @Column(name = "created_at", nullable = false)
  private ZonedDateTime createdAt;

  @Column(name = "updated_at")
  private ZonedDateTime updatedAt;

  public enum Status {
    PLANNED,
    SENT,
    SKIPPED
  }

  @PrePersist
  protected void onCreate() {
    createdAt = ZonedDateTime.now();
    updatedAt = ZonedDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = ZonedDateTime.now();
  }

  // Constructors
  public PlanItem() {}

  public PlanItem(Long userId, LocalDate date, Integer seq, String topicTitle) {
    this.userId = userId;
    this.date = date;
    this.seq = seq;
    this.topicTitle = topicTitle;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

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

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(ZonedDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public ZonedDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(ZonedDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
