package com.example.moduleproduct.usecase.product;


import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.product.ProductDomainService;
import com.example.moduleproduct.service.productLike.ProductLikeDataService;
import com.example.moduleproduct.service.productLike.ProductLikeDomainService;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import com.example.moduleuser.service.reader.MemberReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductWriteUseCase {

    private final ProductDataService productDataService;

    private final ProductDomainService productDomainService;

    private final ProductQnADataService productQnADataService;

    private final ProductLikeDataService productLikeDataService;

    private final ProductLikeDomainService productLikeDomainService;

    private final MemberReader memberReader;

    public String postProductQnA(ProductQnAPostDTO postDTO, String userId) {
        Member member = memberReader.getMemberByUserIdOrElseNull(userId);
        if(member == null)
            throw new IllegalArgumentException();
        Product product = productDataService.getProductById(postDTO.productId());
        ProductQnA productQnA = postDTO.toProductQnAEntity(member, product);

        productQnADataService.saveProductQnA(productQnA);

        return Result.OK.getResultKey();
    }

    public String likeProduct(String productId, String userId) {
        ProductLike productLike = getProductLike(productId, userId);

        productLikeDataService.saveProductLike(productLike);

        return Result.OK.getResultKey();
    }

    public String deleteProductLike(String productId, String userId) {
        ProductLike productLike = getProductLike(productId, userId);

        productLikeDataService.deleteProductLike(productLike);

        return Result.OK.getResultKey();
    }

    private ProductLike getProductLike(String productId, String userId) {
        if(userId == null)
            throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage());

        Member member = memberReader.getMemberByUserIdOrElseAccessDenied(userId);
        Product product = productDataService.getProductById(productId);

        return productLikeDomainService.buildLikeProduct(member, product);
    }
}
