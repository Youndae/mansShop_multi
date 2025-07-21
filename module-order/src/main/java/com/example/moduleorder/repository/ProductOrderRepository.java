package com.example.moduleorder.repository;

import com.example.modulecommon.model.entity.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long>, ProductOrderDSLRepository{
}
