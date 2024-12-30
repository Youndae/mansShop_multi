package com.example.moduleproduct.repository.productReview;

import com.example.moduleproduct.model.dto.product.business.ProductReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductReviewDSLRepository {

    Page<ProductReviewResponseDTO> findByProductId(String productId, Pageable pageable);
}
