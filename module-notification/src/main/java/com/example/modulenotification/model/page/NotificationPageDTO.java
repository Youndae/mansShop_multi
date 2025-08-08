package com.example.modulenotification.model.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record NotificationPageDTO(
        int pageNum,
        int amount
) {

    public NotificationPageDTO(int page) {
        this(page, PageAmount.DEFAULT_AMOUNT.getAmount());
    }
}
