package com.knowledge.topic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(columnDefinition = "TEXT")
    private String overviewContent;

    @Column(columnDefinition = "TEXT")
    private String detailedContent;

    @Column(name = "overview_word_count")
    private Integer overviewWordCount;

    @Column(name = "detailed_word_count")
    private Integer detailedWordCount;

    @Column(name = "generation_time_seconds")
    private Integer generationTimeSeconds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "email_sent")
    private Boolean emailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public KnowledgeContent(Topic topic, String overviewContent, String detailedContent) {
        this.topic = topic;
        this.overviewContent = overviewContent;
        this.detailedContent = detailedContent;
        this.overviewWordCount = overviewContent != null ? overviewContent.split("\\s+").length : 0;
        this.detailedWordCount = detailedContent != null ? detailedContent.split("\\s+").length : 0;
        this.emailSent = false;
    }
}
