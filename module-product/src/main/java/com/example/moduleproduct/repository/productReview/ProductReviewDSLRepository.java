package com.example.moduleproduct.repository.productReview;


import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDetailDTO;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPageReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductReviewDSLRepository {

    Page<ProductDetailReviewDTO> findByProductId(String productId, Pageable pageable);

    Page<MyPageReviewDTO> findAllByUserId(String userId, Pageable pageable);

    List<AdminReviewDTO> findAllByAdminReviewList(AdminReviewPageDTO pageDTO, String listType);

    Long countByAdminReviewList(AdminReviewPageDTO pageDTO, String listType);

    AdminReviewDetailDTO findAdminReviewDetailById(long reviewId);
}
