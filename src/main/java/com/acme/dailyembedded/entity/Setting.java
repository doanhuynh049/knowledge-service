package com.acme.dailyembedded.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Setting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "email_mode")
  private EmailMode emailMode = EmailMode.DIGEST_AND_SPLIT;

  @Column(name = "max_deep_dives_per_day")
  private Integer maxDeepDivesPerDay = 5;

  private String model = "gpt-3.5-turbo";

  private Double temperature = 0.7;

  @Column(name = "max_tokens")
  private Integer maxTokens = 2000;

  @Column(name = "cc_list_json", columnDefinition = "TEXT")
  private String ccListJson = "[]";

  public enum EmailMode {
    DIGEST_AND_SPLIT,
    SINGLE
  }

  // Constructors
  public Setting() {}

  public Setting(Long userId) {
    this.userId = userId;
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

  public EmailMode getEmailMode() {
    return emailMode;
  }

  public void setEmailMode(EmailMode emailMode) {
    this.emailMode = emailMode;
  }

  public Integer getMaxDeepDivesPerDay() {
    return maxDeepDivesPerDay;
  }

  public void setMaxDeepDivesPerDay(Integer maxDeepDivesPerDay) {
    this.maxDeepDivesPerDay = maxDeepDivesPerDay;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public String getCcListJson() {
    return ccListJson;
  }

  public void setCcListJson(String ccListJson) {
    this.ccListJson = ccListJson;
  }
}
