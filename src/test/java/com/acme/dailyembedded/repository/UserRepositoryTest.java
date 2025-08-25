package com.acme.dailyembedded.repository;

import com.acme.dailyembedded.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldSaveAndFindUserByEmail() {
    // Given
    User user = new User("test@example.com", "America/New_York", 9);

    // When
    User savedUser = userRepository.save(user);
    Optional<User> foundUser = userRepository.findByEmail("test@example.com");

    // Then
    assertThat(savedUser.getId()).isNotNull();
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    assertThat(foundUser.get().getTimezone()).isEqualTo("America/New_York");
    assertThat(foundUser.get().getDeliveryHourLocal()).isEqualTo(9);
  }

  @Test
  void shouldReturnEmptyWhenUserNotFound() {
    // When
    Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

    // Then
    assertThat(foundUser).isEmpty();
  }

  @Test
  void shouldCheckUserExistenceByEmail() {
    // Given
    User user = new User("exists@example.com", "UTC", 10);
    userRepository.save(user);

    // When & Then
    assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
    assertThat(userRepository.existsByEmail("not-exists@example.com")).isFalse();
  }
}
