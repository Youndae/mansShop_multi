package com.example.moduleuser.model.dto.admin.page;

import com.example.modulecommon.model.dto.request.ListRequestDTO;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleuser.model.enumuration.AdminMemberSearchType;

public record AdminMemberPageDTO(
        String keyword,
        String searchType,
        int page,
        int amount,
        long offset
) {

    private static final PageAmount pageAmount = PageAmount.DEFAULT_AMOUNT;

    public static AdminMemberPageDTO fromRequestDTO(ListRequestDTO requestDTO, String searchType) {
        int page = PaginationUtils.getRequestPageValue(requestDTO.page());
        String searchTypeValue = searchType == null ? null : AdminMemberSearchType.from(searchType).value();
        long offset = PaginationUtils.getOffsetOperation(page, pageAmount);

        return new AdminMemberPageDTO(
                requestDTO.keyword(),
                searchTypeValue,
                page,
                pageAmount.getAmount(),
                offset
        );
    }

    public AdminMemberPageDTO(String keyword, String searchType, int page) {
        this(
                keyword,
                searchType,
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }

    public AdminMemberPageDTO(int page) {
        this(
                null,
                null,
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }
}
