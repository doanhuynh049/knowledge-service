package com.acme.dailyembedded.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${app.api.key}")
  private String apiKey;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/health", "/actuator/health").permitAll()
            .requestMatchers("/lessons/**").permitAll()  // Allow Thymeleaf lesson view
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
        )
        .addFilterBefore(new ApiKeyFilter(apiKey), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  public static class ApiKeyFilter implements Filter {

    private final String validApiKey;

    public ApiKeyFilter(String validApiKey) {
      this.validApiKey = validApiKey;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      String requestURI = httpRequest.getRequestURI();

      // Skip API key check for public endpoints
      if (!requestURI.startsWith("/api/") ||
          requestURI.equals("/actuator/health") ||
          requestURI.equals("/health")) {
        chain.doFilter(request, response);
        return;
      }

      String providedApiKey = httpRequest.getHeader("X-API-Key");

      if (validApiKey == null || validApiKey.isEmpty()) {
        httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        httpResponse.getWriter().write("{\"error\":\"API key not configured\"}");
        return;
      }

      if (providedApiKey == null || !validApiKey.equals(providedApiKey)) {
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("{\"error\":\"Invalid API key\"}");
        return;
      }

      chain.doFilter(request, response);
    }
  }
}
