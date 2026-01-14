package com.example.moduleuser.model.dto.admin.page;

import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.moduleuser.model.dto.admin.in.AdminMemberListRequestDTO;
import com.example.moduleuser.model.enumuration.AdminMemberSearchType;

public record AdminMemberPageDTO(
        String keyword,
        String searchType,
        int page,
        int amount,
        long offset
) {

    public static AdminMemberPageDTO fromRequestDTO(AdminMemberListRequestDTO requestDTO, String searchType) {
        int page = setPageValue(requestDTO.page());

        // keyword가 없는데 SearchType이 있거나
        // keyword는 있는데 SearchType이 없는 경우
        // 정상적인 요청이 아니므로 IllegalArgumentException으로 단순 BAD_REQUEST를 반환
        if((requestDTO.keyword() == null && searchType != null)
                || (requestDTO.keyword() != null && searchType == null)) {
            throw new IllegalArgumentException("AdminMemberPageDTO.fromRequestDTO :: keyword and searchType must both be null or both must exist. keyword=" + requestDTO.keyword() + ", searchType=" + searchType);
        }

        String searchTypeValue = searchType == null ? null : AdminMemberSearchType.from(searchType).getValue();

        return new AdminMemberPageDTO(
                requestDTO.keyword(),
                searchTypeValue,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                offsetOperation(page)
        );
    }

    private static int setPageValue(Integer page) {
        return page == null ? 1 : page;
    }

    private static long offsetOperation(int page) {

        return (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount();
    }

    public AdminMemberPageDTO(String keyword, String searchType, int page) {
        this(
                keyword,
                searchType,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }

    public AdminMemberPageDTO(int page) {
        this(
                null,
                null,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }
}
