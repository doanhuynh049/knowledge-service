package com.acme.dailyembedded.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "email_logs")
public class EmailLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "lesson_id")
  private Long lessonId;

  @Enumerated(EnumType.STRING)
  private Type type;

  @Column(name = "to_email", nullable = false)
  private String toEmail;

  @Enumerated(EnumType.STRING)
  private Status status;

  @Column(name = "provider_msg_id")
  private String providerMsgId;

  @Column(columnDefinition = "TEXT")
  private String error;

  @Column(name = "sent_at")
  private ZonedDateTime sentAt;

  public enum Type {
    OVERVIEW,
    DEEPDIVE,
    DIGEST
  }

  public enum Status {
    SENT,
    FAILED
  }

  // Constructors
  public EmailLog() {}

  public EmailLog(Long lessonId, Type type, String toEmail) {
    this.lessonId = lessonId;
    this.type = type;
    this.toEmail = toEmail;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getLessonId() {
    return lessonId;
  }

  public void setLessonId(Long lessonId) {
    this.lessonId = lessonId;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getToEmail() {
    return toEmail;
  }

  public void setToEmail(String toEmail) {
    this.toEmail = toEmail;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getProviderMsgId() {
    return providerMsgId;
  }

  public void setProviderMsgId(String providerMsgId) {
    this.providerMsgId = providerMsgId;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public ZonedDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(ZonedDateTime sentAt) {
    this.sentAt = sentAt;
  }
}
