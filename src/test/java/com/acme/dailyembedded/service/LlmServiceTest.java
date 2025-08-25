package com.acme.dailyembedded.service;

import com.acme.dailyembedded.entity.Setting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

  @Mock
  private LlmService llmService;

  @Test
  void shouldGenerateContentSuccessfully() {
    // Given
    String systemPrompt = "You are an embedded systems expert";
    String userPrompt = "Explain UART communication";
    String model = "gpt-3.5-turbo";
    double temperature = 0.7;
    int maxTokens = 1000;
    String expectedResponse = "# UART Communication\n\nUART (Universal Asynchronous Receiver-Transmitter) is...";

    when(llmService.generateContent(systemPrompt, userPrompt, model, temperature, maxTokens))
        .thenReturn(expectedResponse);

    // When
    String result = llmService.generateContent(systemPrompt, userPrompt, model, temperature, maxTokens);

    // Then
    assertThat(result).isEqualTo(expectedResponse);
    assertThat(result).contains("UART");
    assertThat(result).contains("#");
  }

  @Test
  void shouldHandleLlmServiceFailure() {
    // Given
    when(llmService.generateContent(anyString(), anyString(), anyString(), anyDouble(), anyInt()))
        .thenThrow(new RuntimeException("OpenAI API call failed: 429"));

    // When & Then
    assertThatThrownBy(() ->
        llmService.generateContent("system", "user", "gpt-3.5-turbo", 0.7, 1000))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("OpenAI API call failed");
  }
}
