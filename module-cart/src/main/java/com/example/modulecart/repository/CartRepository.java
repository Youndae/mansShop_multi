package com.example.modulecart.repository;

import com.example.modulecommon.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long>, CartDSLRepository {
}
