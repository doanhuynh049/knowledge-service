package com.acme.dailyembedded.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

  private static final Logger logger = LoggerFactory.getLogger(LlmService.class);

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String providerUrl;
  private final String providerType;

  public LlmService(
      @Value("${app.llm.api-key}") String apiKey,
      @Value("${app.llm.provider-url}") String providerUrl,
      @Value("${app.llm.provider-type:gemini}") String providerType) {
    this.apiKey = apiKey;
    this.providerUrl = providerUrl;
    this.providerType = providerType;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();
    this.objectMapper = new ObjectMapper();
  }

  public String generateContent(String systemPrompt, String userPrompt, String model, double temperature, int maxTokens) {
    try {
      if ("gemini".equalsIgnoreCase(providerType)) {
        return generateGeminiContent(systemPrompt, userPrompt, temperature, maxTokens);
      } else {
        return generateOpenAiContent(systemPrompt, userPrompt, model, temperature, maxTokens);
      }
    } catch (Exception e) {
      logger.error("Error calling LLM API", e);
      throw new RuntimeException("Failed to generate content from LLM", e);
    }
  }

  private String generateGeminiContent(String systemPrompt, String userPrompt, double temperature, int maxTokens) throws Exception {
    var request = new GeminiRequest(
        List.of(new GeminiPart(systemPrompt + "\n\n" + userPrompt)),
        new GeminiGenerationConfig(temperature, maxTokens)
    );

    String requestBody = objectMapper.writeValueAsString(request);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(providerUrl + "?key=" + apiKey))
        .header("Content-Type", "application/json")
        .timeout(Duration.ofMinutes(2))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();

    HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      logger.error("Gemini API error: {} - {}", response.statusCode(), response.body());
      throw new RuntimeException("Gemini API call failed: " + response.statusCode());
    }

    GeminiResponse geminiResponse = objectMapper.readValue(response.body(), GeminiResponse.class);

    if (geminiResponse.candidates == null || geminiResponse.candidates.isEmpty() ||
        geminiResponse.candidates.get(0).content == null ||
        geminiResponse.candidates.get(0).content.parts == null ||
        geminiResponse.candidates.get(0).content.parts.isEmpty()) {
      throw new RuntimeException("No content returned from Gemini API");
    }

    return geminiResponse.candidates.get(0).content.parts.get(0).text;
  }

  private String generateOpenAiContent(String systemPrompt, String userPrompt, String model, double temperature, int maxTokens) throws Exception {
    var request = new OpenAiRequest(
        model,
        List.of(
            new OpenAiMessage("system", systemPrompt),
            new OpenAiMessage("user", userPrompt)
        ),
        maxTokens,
        temperature
    );

    String requestBody = objectMapper.writeValueAsString(request);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://api.openai.com/v1/chat/completions"))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .timeout(Duration.ofMinutes(2))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();

    HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      logger.error("OpenAI API error: {} - {}", response.statusCode(), response.body());
      throw new RuntimeException("OpenAI API call failed: " + response.statusCode());
    }

    OpenAiResponse openAiResponse = objectMapper.readValue(response.body(), OpenAiResponse.class);

    if (openAiResponse.choices == null || openAiResponse.choices.isEmpty()) {
      throw new RuntimeException("No choices returned from OpenAI API");
    }

    return openAiResponse.choices.get(0).message.content;
  }

  // Gemini API DTOs
  private record GeminiRequest(
      List<GeminiPart> contents,
      @JsonProperty("generationConfig") GeminiGenerationConfig generationConfig
  ) {}

  private record GeminiPart(String text) {}

  private record GeminiGenerationConfig(
      double temperature,
      @JsonProperty("maxOutputTokens") int maxOutputTokens
  ) {}

  private record GeminiResponse(List<GeminiCandidate> candidates) {}

  private record GeminiCandidate(GeminiContent content) {}

  private record GeminiContent(List<GeminiPart> parts) {}

  // OpenAI API DTOs
  private record OpenAiRequest(
      String model,
      List<OpenAiMessage> messages,
      @JsonProperty("max_tokens") int maxTokens,
      double temperature
  ) {}

  private record OpenAiMessage(String role, String content) {}

  private record OpenAiResponse(List<OpenAiChoice> choices) {}

  private record OpenAiChoice(OpenAiMessage message) {}
}
