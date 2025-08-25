package com.acme.dailyembedded.repository;

import com.acme.dailyembedded.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

  List<EmailLog> findByLessonIdOrderBySentAtDesc(Long lessonId);

  List<EmailLog> findByToEmailOrderBySentAtDesc(String toEmail);

  List<EmailLog> findByStatusOrderBySentAtDesc(EmailLog.Status status);
}
