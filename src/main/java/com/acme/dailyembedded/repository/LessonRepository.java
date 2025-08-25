package com.acme.dailyembedded.repository;

import com.acme.dailyembedded.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

  List<Lesson> findByUserIdAndDateOrderBySeq(Long userId, LocalDate date);

  Optional<Lesson> findByUserIdAndDateAndSeq(Long userId, LocalDate date, Integer seq);

  boolean existsByUserIdAndDateAndSeq(Long userId, LocalDate date, Integer seq);
}
