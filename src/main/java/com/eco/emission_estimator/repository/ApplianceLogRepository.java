package com.eco.emission_estimator.repository;

import com.eco.emission_estimator.model.ApplianceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplianceLogRepository extends JpaRepository<ApplianceLog, Long> {
    List<ApplianceLog> findByUserIdOrderByMonthYearAsc(Long userId);
    List<ApplianceLog> findByUserIdAndMonthYear(Long userId, String monthYear);
}