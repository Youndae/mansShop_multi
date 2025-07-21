package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record ProductDetailPageDTO(
        int pageNum,
        int reviewAmount,
        int qnaAmount
) {

    public ProductDetailPageDTO() {
        this(1, PageAmount.PRODUCT_REVIEW_AMOUNT.getAmount(), PageAmount.PRODUCT_QNA_AMOUNT.getAmount());
    }

    public ProductDetailPageDTO(int page) {
        this(
                page,
                PageAmount.PRODUCT_REVIEW_AMOUNT.getAmount(),
                PageAmount.PRODUCT_QNA_AMOUNT.getAmount()
        );
    }
}
