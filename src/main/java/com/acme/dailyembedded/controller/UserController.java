package com.acme.dailyembedded.controller;

import com.acme.dailyembedded.dto.UpsertUserRequest;
import com.acme.dailyembedded.entity.Setting;
import com.acme.dailyembedded.entity.User;
import com.acme.dailyembedded.repository.SettingRepository;
import com.acme.dailyembedded.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserRepository userRepository;
  private final SettingRepository settingRepository;

  public UserController(UserRepository userRepository, SettingRepository settingRepository) {
    this.userRepository = userRepository;
    this.settingRepository = settingRepository;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> upsertUser(@Valid @RequestBody UpsertUserRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElse(new User());

    user.setEmail(request.getEmail());
    user.setTimezone(request.getTimezone());
    user.setDeliveryHourLocal(request.getDeliveryHourLocal());
    user.setPrefsJson(request.getPrefsJson());

    User savedUser = userRepository.save(user);

    // Create default settings if they don't exist
    if (!settingRepository.existsByUserId(savedUser.getId())) {
      Setting defaultSettings = new Setting(savedUser.getId());
      settingRepository.save(defaultSettings);
    }

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("user", Map.of(
        "id", savedUser.getId(),
        "email", savedUser.getEmail(),
        "timezone", savedUser.getTimezone(),
        "deliveryHourLocal", savedUser.getDeliveryHourLocal(),
        "createdAt", savedUser.getCreatedAt()
    ));

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
    return userRepository.findById(id)
        .map(user -> {
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("user", Map.of(
              "id", user.getId(),
              "email", user.getEmail(),
              "timezone", user.getTimezone(),
              "deliveryHourLocal", user.getDeliveryHourLocal(),
              "prefsJson", user.getPrefsJson(),
              "createdAt", user.getCreatedAt()
          ));
          return ResponseEntity.ok(response);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-email/{email}")
  public ResponseEntity<Map<String, Object>> getUserByEmail(@PathVariable String email) {
    return userRepository.findByEmail(email)
        .map(user -> {
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("user", Map.of(
              "id", user.getId(),
              "email", user.getEmail(),
              "timezone", user.getTimezone(),
              "deliveryHourLocal", user.getDeliveryHourLocal(),
              "prefsJson", user.getPrefsJson(),
              "createdAt", user.getCreatedAt()
          ));
          return ResponseEntity.ok(response);
        })
        .orElse(ResponseEntity.notFound().build());
  }
}
