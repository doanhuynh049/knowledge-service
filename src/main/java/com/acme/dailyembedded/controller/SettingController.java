package com.acme.dailyembedded.controller;

import com.acme.dailyembedded.entity.Setting;
import com.acme.dailyembedded.repository.SettingRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingController {

  private final SettingRepository settingRepository;

  public SettingController(SettingRepository settingRepository) {
    this.settingRepository = settingRepository;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> upsertSettings(@Valid @RequestBody UpsertSettingRequest request) {
    Setting setting = settingRepository.findByUserId(request.getUserId())
        .orElse(new Setting(request.getUserId()));

    setting.setEmailMode(request.getEmailMode());
    setting.setMaxDeepDivesPerDay(request.getMaxDeepDivesPerDay());
    setting.setModel(request.getModel());
    setting.setTemperature(request.getTemperature());
    setting.setMaxTokens(request.getMaxTokens());
    setting.setCcListJson(request.getCcListJson());

    Setting savedSetting = settingRepository.save(setting);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("setting", Map.of(
        "id", savedSetting.getId(),
        "userId", savedSetting.getUserId(),
        "emailMode", savedSetting.getEmailMode().toString(),
        "maxDeepDivesPerDay", savedSetting.getMaxDeepDivesPerDay(),
        "model", savedSetting.getModel(),
        "temperature", savedSetting.getTemperature(),
        "maxTokens", savedSetting.getMaxTokens()
    ));

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<Map<String, Object>> getSettings(@PathVariable Long userId) {
    return settingRepository.findByUserId(userId)
        .map(setting -> {
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("setting", Map.of(
              "id", setting.getId(),
              "userId", setting.getUserId(),
              "emailMode", setting.getEmailMode().toString(),
              "maxDeepDivesPerDay", setting.getMaxDeepDivesPerDay(),
              "model", setting.getModel(),
              "temperature", setting.getTemperature(),
              "maxTokens", setting.getMaxTokens(),
              "ccListJson", setting.getCcListJson()
          ));
          return ResponseEntity.ok(response);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  public static class UpsertSettingRequest {
    private Long userId;
    private Setting.EmailMode emailMode = Setting.EmailMode.DIGEST_AND_SPLIT;
    private Integer maxDeepDivesPerDay = 5;
    private String model = "gpt-3.5-turbo";
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private String ccListJson = "[]";

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Setting.EmailMode getEmailMode() { return emailMode; }
    public void setEmailMode(Setting.EmailMode emailMode) { this.emailMode = emailMode; }

    public Integer getMaxDeepDivesPerDay() { return maxDeepDivesPerDay; }
    public void setMaxDeepDivesPerDay(Integer maxDeepDivesPerDay) { this.maxDeepDivesPerDay = maxDeepDivesPerDay; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public String getCcListJson() { return ccListJson; }
    public void setCcListJson(String ccListJson) { this.ccListJson = ccListJson; }
  }
}
