package com.example.moduleproduct.repository.productInfoImage;

import com.example.modulecommon.model.entity.ProductInfoImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductInfoImageRepository extends JpaRepository<ProductInfoImage, Long>, ProductInfoImageDSLRepository {
}
