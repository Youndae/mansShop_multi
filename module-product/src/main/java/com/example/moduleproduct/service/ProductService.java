package com.example.moduleproduct.service;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.*;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailDTO;
import com.example.moduleproduct.model.dto.product.out.ProductPageableDTO;
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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductOptionRepository productOptionRepository;

    private final ProductLikeRepository productLikeRepository;

    private final ProductThumbnailRepository productThumbnailRepository;

    private final ProductInfoImageRepository productInfoImageRepository;

    private final ProductReviewRepository productReviewRepository;

    private final ProductQnARepository productQnARepository;

    private final ProductQnAReplyRepository productQnAReplyRepository;

    private final MemberRepository memberRepository;


    public ProductDetailDTO getProductDetail(String productId, String userId) {
        Product product = getProduct(productId);

        boolean likeStat = false;

        if(userId != null)
            likeStat = getUserLikeStatus(productId, userId) == 1;

        List<ProductOptionDTO> optionList = productOptionRepository.findByDetailOption(productId);
        List<String> thumbnailList = getProductThumbnailList(productId);
        List<String> infoImageList = getProductInfoImageList(productId);

        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        ProductPageableDTO<ProductReviewResponseDTO> review = getDetailReview(pageDTO, productId);
        ProductPageableDTO<ProductQnAResponseDTO> qna = getDetailQnA(pageDTO, productId);

        return new ProductDetailDTO(
                        product,
                        likeStat,
                        optionList,
                        thumbnailList,
                        infoImageList,
                        review,
                        qna
                );
    }

    private Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new CustomNotFoundException(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage())
                );
    }

    private int getUserLikeStatus(String productId, String userId) {
        return productLikeRepository.countByUserIdAndProductId(productId, userId);
    }

    private List<String> getProductThumbnailList(String productId) {
        return productThumbnailRepository.findByProductId(productId);
    }

    private List<String> getProductInfoImageList(String productId) {
        return productInfoImageRepository.findByProductId(productId);
    }

    public ProductPageableDTO<ProductReviewResponseDTO> getDetailReview(ProductDetailPageDTO pageDTO, String productId) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                                        pageDTO.reviewAmount(),
                                                        Sort.by("createdAt").descending()
                                                );

        Page<ProductReviewResponseDTO> data = productReviewRepository.findByProductId(productId, pageable);

        return new ProductPageableDTO<>(data);
    }

    public ProductPageableDTO<ProductQnAResponseDTO> getDetailQnA(ProductDetailPageDTO pageDTO, String productId) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                                    pageDTO.qnaAmount(),
                                                    Sort.by("createdAt").descending()
                                            );
        Page<ProductQnADTO> productQnA = productQnARepository.findByProductId(productId, pageable);
        List<Long> qnaIds = productQnA.getContent().stream().map(ProductQnADTO::qnaId).toList();
        List<ProductQnAResponseDTO> responseDTOList = new ArrayList<>();
        List<ProductQnAReply> replyList = productQnAReplyRepository.findByQnAReply(qnaIds);
        int replyIdx = 0;

        for(int i = 0; i < productQnA.getContent().size(); i++) {
            List<ProductQnAReplyDTO> replyDTOList = new ArrayList<>();
            ProductQnADTO dto = productQnA.getContent().get(i);

            for(int j = replyIdx; j < replyList.size(); j++) {
                if(dto.qnaId() == replyList.get(j).getProductQnA().getId())
                    replyDTOList.add(new ProductQnAReplyDTO(replyList.get(j)));
                else{
                    replyIdx = j;
                    break;
                }
            }
            responseDTOList.add(new ProductQnAResponseDTO(dto, replyDTOList));
        }

        return new ProductPageableDTO<>(new PageImpl<>(responseDTOList, pageable, productQnA.getTotalElements()));
    }

    public String likeProduct(String productId, String userId) {
        ProductLike productLike = setLikeProduct(productId, userId);

        productLikeRepository.save(productLike);

        return Result.OK.getResultKey();
    }

    public String deLikeProduct(String productId, String userId) {
        ProductLike productLike = setLikeProduct(productId, userId);

        productLikeRepository.deleteByUserIdAndProductId(productLike);

        return Result.OK.getResultKey();
    }

    private ProductLike setLikeProduct(String productId, String userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomNotFoundException(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage()));
        Product product = productRepository.findById(productId).orElseThrow(() -> new CustomNotFoundException(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage()));

        return ProductLike.builder()
                .member(member)
                .product(product)
                .build();
    }

    public String postProductQnA(ProductQnAPostDTO postDTO, String userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomNotFoundException(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage()));
        Product product = productRepository.findById(postDTO.productId()).orElseThrow(() -> new CustomNotFoundException(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage()));
        ProductQnA productQnA = postDTO.toProductQnAEntity(member, product);
        productQnARepository.save(productQnA);

        return Result.OK.getResultKey();
    }
}
