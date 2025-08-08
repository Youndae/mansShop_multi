package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record LikePageDTO(
        int pageNum,
        int amount
) {

    public LikePageDTO(int page) {
        this(page, PageAmount.PRODUCT_LIKE_AMOUNT.getAmount());
    }
}
