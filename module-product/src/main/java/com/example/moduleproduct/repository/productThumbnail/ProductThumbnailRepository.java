package com.example.moduleproduct.repository.productThumbnail;

import com.example.modulecommon.model.entity.ProductThumbnail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductThumbnailRepository extends JpaRepository<ProductThumbnail, Long>, ProductThumbnailDSLRepository {
}
