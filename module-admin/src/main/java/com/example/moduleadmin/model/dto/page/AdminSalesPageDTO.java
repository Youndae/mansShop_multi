package com.example.moduleadmin.model.dto.page;

import com.example.modulecommon.model.dto.request.ListRequestDTO;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.modulecommon.utils.PaginationUtils;

public record AdminSalesPageDTO(
        String keyword,
        int page,
        int amount,
        long offset
) {

    private static final PageAmount pageAmount = PageAmount.DEFAULT_AMOUNT;

    public static AdminSalesPageDTO fromRequestDTO(ListRequestDTO requestDTO) {
        int page = PaginationUtils.getRequestPageValue(requestDTO.page());
        long offset = PaginationUtils.getOffsetOperation(page, pageAmount);
        String keyword = requestDTO.keyword() == null ? null : "%" + requestDTO.keyword() + "%";
        return new AdminSalesPageDTO(
                keyword,
                page,
                pageAmount.getAmount(),
                offset
        );
    }

    public AdminSalesPageDTO(String keyword, int page) {
        this(
                keyword == null ? null : "%" + keyword + "%",
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }
}
