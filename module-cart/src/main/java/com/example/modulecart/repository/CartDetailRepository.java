package com.example.modulecart.repository;

import com.example.modulecommon.model.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartDetailRepository extends JpaRepository<CartDetail, Long>, CartDetailDSLRepository {
}
