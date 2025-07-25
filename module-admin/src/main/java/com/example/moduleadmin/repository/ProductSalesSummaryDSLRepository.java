package com.example.moduleadmin.repository;

import com.example.modulecommon.model.entity.ProductSalesSummary;

import java.time.LocalDate;
import java.util.List;

public interface ProductSalesSummaryDSLRepository {

    List<ProductSalesSummary> findAllByProductOptionIds(LocalDate periodMonth, List<Long> productOptionIds);
}
