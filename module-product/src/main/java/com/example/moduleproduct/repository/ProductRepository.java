package com.example.moduleproduct.repository;

import com.example.modulecommon.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String>, ProductDSLRepository {
}
