package com.example.moduleorder.repository;

import com.example.modulecommon.model.entity.ProductOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderDetailRepository extends JpaRepository<ProductOrderDetail, Long>, ProductOrderDetailDSLRepository {
}
