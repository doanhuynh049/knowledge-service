package com.acme.dailyembedded.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String timezone = "Asia/Ho_Chi_Minh";

  @Column(name = "delivery_hour_local", nullable = false)
  private Integer deliveryHourLocal = 6; // Changed from 9 to 6 AM

  @Column(name = "prefs_json", columnDefinition = "TEXT")
  private String prefsJson = "{}";

  @Column(name = "created_at", nullable = false)
  private ZonedDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = ZonedDateTime.now();
  }

  // Constructors
  public User() {}

  public User(String email, String timezone, Integer deliveryHourLocal) {
    this.email = email;
    this.timezone = timezone;
    this.deliveryHourLocal = deliveryHourLocal != null ? deliveryHourLocal : 6;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public Integer getDeliveryHourLocal() {
    return deliveryHourLocal;
  }

  public void setDeliveryHourLocal(Integer deliveryHourLocal) {
    this.deliveryHourLocal = deliveryHourLocal;
  }

  public String getPrefsJson() {
    return prefsJson;
  }

  public void setPrefsJson(String prefsJson) {
    this.prefsJson = prefsJson;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(ZonedDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
