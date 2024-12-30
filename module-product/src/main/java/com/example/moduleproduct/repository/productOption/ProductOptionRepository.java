package com.example.moduleproduct.repository.productOption;

import com.example.modulecommon.model.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long>, ProductOptionDSLRepository {
}
