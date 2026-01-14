package com.example.moduleorder.model.dto.admin.page;

import com.example.modulecommon.model.dto.request.ListRequestDTO;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleorder.model.enumuration.AdminOrderSearchType;

public record AdminOrderPageDTO(
        String keyword,
        String searchType,
        int page,
        int amount,
        long offset
) {

    private static final PageAmount pageAmount = PageAmount.DEFAULT_AMOUNT;

    public static AdminOrderPageDTO fromRequestDTO(ListRequestDTO requestDTO, String searchType) {
        int page = PaginationUtils.getRequestPageValue(requestDTO.page());
        String searchTypeValue = searchType == null ? null : AdminOrderSearchType.from(searchType).value();
        long offset = PaginationUtils.getOffsetOperation(page, pageAmount);

        return new AdminOrderPageDTO(
                requestDTO.keyword(),
                searchTypeValue,
                page,
                pageAmount.getAmount(),
                offset
        );
    }

    public AdminOrderPageDTO(String keyword, String searchType, int page) {
        this(
                keyword,
                searchType,
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }

    public AdminOrderPageDTO(int page) {
        this(
                null,
                null,
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }
}
