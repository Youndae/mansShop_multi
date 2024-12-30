package com.example.moduleproduct.repository.product;

import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.repository.product.ProductDSLRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String>, ProductDSLRepository {
}
