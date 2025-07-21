package com.example.moduleadmin.repository;

import com.example.modulecommon.model.entity.PeriodSalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodSalesSummaryRepository extends JpaRepository<PeriodSalesSummary, Long>, PeriodSalesSummaryDSLRepository {
}
