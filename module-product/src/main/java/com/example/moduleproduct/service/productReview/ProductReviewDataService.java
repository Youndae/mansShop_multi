package com.example.moduleproduct.service.productReview;

import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.entity.ProductReview;
import com.example.modulecommon.model.entity.ProductReviewReply;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDetailDTO;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPageReviewDTO;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewDataService {

    private final ProductReviewRepository productReviewRepository;

    private final ProductReviewReplyRepository productReviewReplyRepository;

    public Page<ProductDetailReviewDTO> getProductDetailReview(ProductDetailPageDTO pageDTO,
                                                               String productId) {
        Pageable reviewPageable = PageRequest.of(pageDTO.pageNum() - 1,
                pageDTO.reviewAmount(),
                Sort.by("createdAt").descending()
        );

        return productReviewRepository.findByProductId(productId, reviewPageable);
    }

    public Page<MyPageReviewDTO> findAllReviewPaginationByUserId(MyPagePageDTO pageDTO, String userId) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                                pageDTO.amount(),
                                                Sort.by("id").descending()
                                        );

        return productReviewRepository.findAllByUserId(userId, pageable);
    }

    public ProductReview findProductReviewByIdOrElseIllegal(long reviewId) {
        return productReviewRepository.findById(reviewId).orElseThrow(IllegalArgumentException::new);
    }

    public void saveProductReview(ProductReview productReview) {
        productReviewRepository.save(productReview);
    }

    public void deleteProductReview(long reviewId) {
        productReviewRepository.deleteById(reviewId);
    }

    public List<AdminReviewDTO> getAdminProductReviewList(AdminReviewPageDTO pageDTO, String listType) {
        return productReviewRepository.findAllByAdminReviewList(pageDTO, listType);
    }

    public Long countByAdminReviewList(AdminReviewPageDTO pageDTO, String listType) {
        return productReviewRepository.countByAdminReviewList(pageDTO, listType);
    }

    public AdminReviewDetailDTO getAdminReviewDetailById(long reviewId) {
        return productReviewRepository.findAdminReviewDetailById(reviewId);
    }

    public ProductReviewReply getProductReviewReplyByReviewId(long reviewId) {
        return productReviewReplyRepository.findByReviewId(reviewId);
    }

    public void saveProductReviewReply(ProductReviewReply productReviewReply) {
        productReviewReplyRepository.save(productReviewReply);
    }
}
