package com.example.moduleproduct.model.dto.product.in;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductQnA;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "상품 문의 등록 요청 데이터")
public record ProductQnAPostDTO(

        @Schema(name = "productId", description = "상품 아이디")
        @NotBlank(message = "Integrity productId")
        String productId,

        @Schema(name = "content", description = "상품 문의 내용")
        @NotBlank(message = "상품 문의는 필수 입력 사항입니다.")
        @Size(min = 2, message = "상품 문의는 최소 2글자 이상 작성해야 합니다.")
        String content
) {

    public ProductQnA toProductQnAEntity(Member member, Product product) {
        return ProductQnA.builder()
                .member(member)
                .product(product)
                .qnaContent(this.content)
                .build();
    }
}
