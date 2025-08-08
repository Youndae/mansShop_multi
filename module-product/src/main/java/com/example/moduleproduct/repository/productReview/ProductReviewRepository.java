package com.example.moduleproduct.repository.productReview;

import com.example.modulecommon.model.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long>, ProductReviewDSLRepository {

    // ProductReviewService Integration Test QueryMethod
    ProductReview findFirstByMember_UserIdOrderByIdDesc(String memberUserId);
}
