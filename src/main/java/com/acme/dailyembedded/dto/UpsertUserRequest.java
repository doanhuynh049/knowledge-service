package com.acme.dailyembedded.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UpsertUserRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Timezone is required")
  private String timezone;

  @NotNull(message = "Delivery hour is required")
  @Min(value = 0, message = "Delivery hour must be between 0-23")
  @Max(value = 23, message = "Delivery hour must be between 0-23")
  private Integer deliveryHourLocal;

  private String prefsJson = "{}";

  // Constructors
  public UpsertUserRequest() {}

  public UpsertUserRequest(String email, String timezone, Integer deliveryHourLocal) {
    this.email = email;
    this.timezone = timezone;
    this.deliveryHourLocal = deliveryHourLocal;
  }

  // Getters and Setters
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
}
