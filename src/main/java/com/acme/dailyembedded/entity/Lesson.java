package com.acme.dailyembedded.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "lessons")
public class Lesson {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private Integer seq;

  @Column(name = "topic_snapshot_json", columnDefinition = "TEXT")
  private String topicSnapshotJson;

  @Column(name = "overview_md", columnDefinition = "TEXT")
  private String overviewMd;

  @Column(name = "deep_dive_md", columnDefinition = "TEXT")
  private String deepDiveMd;

  @Column(name = "created_at", nullable = false)
  private ZonedDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = ZonedDateTime.now();
  }

  // Constructors
  public Lesson() {}

  public Lesson(Long userId, LocalDate date, Integer seq) {
    this.userId = userId;
    this.date = date;
    this.seq = seq;
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

  public String getTopicSnapshotJson() {
    return topicSnapshotJson;
  }

  public void setTopicSnapshotJson(String topicSnapshotJson) {
    this.topicSnapshotJson = topicSnapshotJson;
  }

  public String getOverviewMd() {
    return overviewMd;
  }

  public void setOverviewMd(String overviewMd) {
    this.overviewMd = overviewMd;
  }

  public String getDeepDiveMd() {
    return deepDiveMd;
  }

  public void setDeepDiveMd(String deepDiveMd) {
    this.deepDiveMd = deepDiveMd;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(ZonedDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
