package com.example.moduleproduct.usecase.productQnA;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQnAWriteUseCase {

    private final ProductQnADataService productQnADataService;

    private final UserDataService userDataService;

    private final ProductDataService productDataService;

    public void postProductQnA(ProductQnAPostDTO postDTO, String userId) {
        Member member = userDataService.getMemberByUserIdOrElseAccessDenied(userId);
        Product product = productDataService.getProductByIdOrElseIllegal(postDTO.productId());
        ProductQnA productQnA = postDTO.toProductQnAEntity(member, product);

        productQnADataService.saveProductQnA(productQnA);
    }

    public void deleteProductQnA(long qnaId, String userId) {
        ProductQnA productQnA = productQnADataService.findProductQnAByIdOrElseIllegal(qnaId);

        if(!productQnA.getMember().getUserId().equals(userId)) {
            log.info("ProductQnAWriteUseCase deleteProductQnA writer not match. requestId = {}, QnAWriter = {}", userId, productQnA.getMember().getUserId());
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        productQnADataService.deleteProductQnAById(qnaId);
    }
}
