package com.acme.dailyembedded;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DailyEmbeddedApplication {

  public static void main(String[] args) {
    SpringApplication.run(DailyEmbeddedApplication.class, args);
  }
}
