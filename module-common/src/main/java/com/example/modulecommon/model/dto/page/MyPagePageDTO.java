package com.example.modulecommon.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record MyPagePageDTO(
        int pageNum,
        int amount
) {

    public MyPagePageDTO(int page) {
        this(page, PageAmount.DEFAULT_AMOUNT.getAmount());
    }
}
