package com.example.moduleproduct.service.product;

import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
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
public class ProductDataService {

    private final ProductRepository productRepository;

    private final ProductOptionRepository productOptionRepository;

    private final ProductThumbnailRepository productThumbnailRepository;

    private final ProductInfoImageRepository productInfoImageRepository;

    private final ProductReviewRepository productReviewRepository;

    private final ProductQnARepository productQnARepository;

    private final ProductQnAReplyRepository productQnAReplyRepository;

    private final ProductLikeRepository productLikeRepository;

    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(IllegalArgumentException::new);
    }

    public boolean getProductLikeStatusByUser(String userId, String productId) {

        return productLikeRepository.countByUserIdAndProductId(productId, userId) == 1;
    }

    public List<ProductOptionDTO> getProductOptionDTOListByProductId(String productId) {
        return productOptionRepository.findByDetailOption(productId);
    }

    public List<String> getProductThumbnailImageNameList(String productId) {
        return productThumbnailRepository.findByProductId(productId);
    }

    public List<String> getProductInfoImageNameList(String productId) {
        return productInfoImageRepository.findByProductId(productId);
    }

    public Page<ProductDetailReviewDTO> getProductDetailReview(ProductDetailPageDTO pageDTO,
                                                               String productId) {
        Pageable reviewPageable = PageRequest.of(pageDTO.pageNum() - 1,
                pageDTO.reviewAmount(),
                Sort.by("createdAt").descending()
        );

        return productReviewRepository.findByProductId(productId, reviewPageable);
    }

    public Page<ProductQnADTO> getProductDetailQnA(Pageable pageable, String productId) {
        return productQnARepository.findByProductId(productId, pageable);
    }

    public List<ProductDetailQnAReplyListDTO> getProductDetailQnAReplyList(List<Long> qnaIds) {
        return productQnAReplyRepository.getQnAReplyListByQnAIds(qnaIds);
    }

    public void saveProductQnA(ProductQnA productQnA) {
        productQnARepository.save(productQnA);
    }

    public void saveProductLike(ProductLike productLike) {
        productLikeRepository.save(productLike);
    }

    public void deleteProductLike(ProductLike productLike) {
        productLikeRepository.deleteByUserIdAndProductId(productLike);
    }
}
