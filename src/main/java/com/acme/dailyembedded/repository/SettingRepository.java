package com.acme.dailyembedded.repository;

import com.acme.dailyembedded.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

  Optional<Setting> findByUserId(Long userId);

  boolean existsByUserId(Long userId);
}
