package com.example.moduleproduct.repository.productReview;


import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductReviewDSLRepository {

    Page<ProductDetailReviewDTO> findByProductId(String productId, Pageable pageable);
}
