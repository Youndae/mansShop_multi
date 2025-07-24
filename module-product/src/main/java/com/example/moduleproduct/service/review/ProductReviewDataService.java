package com.example.moduleproduct.service.review;

import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewDataService {

    private final ProductReviewRepository productReviewRepository;

    public Page<ProductDetailReviewDTO> getProductDetailReview(ProductDetailPageDTO pageDTO,
                                                               String productId) {
        Pageable reviewPageable = PageRequest.of(pageDTO.pageNum() - 1,
                pageDTO.reviewAmount(),
                Sort.by("createdAt").descending()
        );

        return productReviewRepository.findByProductId(productId, reviewPageable);
    }
}
