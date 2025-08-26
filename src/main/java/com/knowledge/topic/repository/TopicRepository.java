package com.knowledge.topic.repository;

import com.knowledge.topic.model.Topic;
import com.knowledge.topic.model.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByName(String name);

    List<Topic> findByStatus(TopicStatus status);

    List<Topic> findByStatusOrderByPriorityDescCreatedAtAsc(TopicStatus status);

    @Query("SELECT t FROM Topic t WHERE t.status = :status AND " +
           "(t.lastProcessed IS NULL OR t.lastProcessed < :cutoffDate) " +
           "ORDER BY t.priority DESC, t.createdAt ASC")
    List<Topic> findUnprocessedTopicsSince(@Param("status") TopicStatus status,
                                          @Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT t FROM Topic t WHERE t.status = :status " +
           "ORDER BY t.priority DESC, t.createdAt ASC")
    List<Topic> findNextTopicsToProcess(@Param("status") TopicStatus status);

    List<Topic> findByCategory(String category);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.status = :status")
    long countByStatus(@Param("status") TopicStatus status);

    @Query("SELECT t FROM Topic t WHERE t.lastProcessed >= :startDate")
    List<Topic> findProcessedSince(@Param("startDate") LocalDateTime startDate);
}
