package com.example.moduleproduct.usecase.productLike;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.productLike.ProductLikeDataService;
import com.example.moduleproduct.service.productLike.ProductLikeDomainService;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductLikeWriteUseCase {

    private final ProductLikeDataService productLikeDataService;

    private final ProductLikeDomainService productLikeDomainService;

    private final UserDataService userDataService;

    private final ProductDataService productDataService;

    public void likeProduct(String productId, String userId) {
        ProductLike productLike = getProductLike(productId, userId);

        productLikeDataService.saveProductLike(productLike);
    }

    public void deleteProductLike(String productId, String userId) {
        ProductLike productLike = getProductLike(productId, userId);

        productLikeDataService.deleteProductLike(productLike);
    }

    private ProductLike getProductLike(String productId, String userId) {
        if(userId == null)
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());

        Member member = userDataService.getMemberByUserIdOrElseAccessDenied(userId);
        Product product = productDataService.getProductByIdOrElseIllegal(productId);

        return productLikeDomainService.buildLikeProduct(member, product);
    }
}
