package com.knowledge.topic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TopicKnowledgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TopicKnowledgeServiceApplication.class, args);
    }
}
