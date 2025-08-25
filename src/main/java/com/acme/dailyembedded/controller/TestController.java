package com.acme.dailyembedded.controller;

import com.acme.dailyembedded.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

  private final MailService mailService;

  public TestController(MailService mailService) {
    this.mailService = mailService;
  }

  @PostMapping("/email")
  public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestBody TestEmailRequest request) {
    try {
      mailService.sendTestEmail(request.getToEmail());

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Test email sent successfully to " + request.getToEmail());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("error", e.getMessage());

      return ResponseEntity.badRequest().body(response);
    }
  }

  public static class TestEmailRequest {
    private String toEmail;

    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }
  }
}
