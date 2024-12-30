package com.example.moduleproduct.repository.productQnA;

import com.example.modulecommon.model.entity.ProductQnA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductQnARepository extends JpaRepository<ProductQnA, Long>, ProductQnADSLRepository {
}
