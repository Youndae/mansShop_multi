package com.example.moduleadmin.repository;

import com.example.modulecommon.model.entity.ProductSalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSalesSummaryRepository extends JpaRepository<ProductSalesSummary, Long> , ProductSalesSummaryDSLRepository {
}
