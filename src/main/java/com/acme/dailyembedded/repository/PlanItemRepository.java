package com.acme.dailyembedded.repository;

import com.acme.dailyembedded.entity.PlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {

  List<PlanItem> findByUserIdAndDateOrderBySeq(Long userId, LocalDate date);

  List<PlanItem> findByUserIdAndDateAndStatusOrderBySeq(Long userId, LocalDate date, PlanItem.Status status);

  Optional<PlanItem> findByUserIdAndDateAndSeq(Long userId, LocalDate date, Integer seq);

  @Query("SELECT p FROM PlanItem p WHERE p.userId = :userId AND p.date = :date AND p.status = 'PLANNED' ORDER BY p.seq")
  List<PlanItem> findPlannedItemsForUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

  boolean existsByUserIdAndDateAndSeq(Long userId, LocalDate date, Integer seq);
}
