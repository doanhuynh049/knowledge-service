package com.knowledge.topic.repository;

import com.knowledge.topic.model.KnowledgeContent;
import com.knowledge.topic.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeContentRepository extends JpaRepository<KnowledgeContent, Long> {

    Optional<KnowledgeContent> findByTopic(Topic topic);

    List<KnowledgeContent> findByEmailSent(Boolean emailSent);

    List<KnowledgeContent> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT kc FROM KnowledgeContent kc WHERE kc.emailSent = false ORDER BY kc.createdAt ASC")
    List<KnowledgeContent> findPendingEmailContent();

    @Query("SELECT AVG(kc.overviewWordCount) FROM KnowledgeContent kc WHERE kc.overviewWordCount > 0")
    Double getAverageOverviewWordCount();

    @Query("SELECT AVG(kc.detailedWordCount) FROM KnowledgeContent kc WHERE kc.detailedWordCount > 0")
    Double getAverageDetailedWordCount();

    @Query("SELECT COUNT(kc) FROM KnowledgeContent kc WHERE kc.createdAt >= :startDate")
    long countGeneratedContentSince(@Param("startDate") LocalDateTime startDate);
}
