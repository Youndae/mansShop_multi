package com.example.moduleapi.model.response;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public record PagingResponseDTO <T>(
        List<T> content,
        boolean empty,
        long number,
        long totalPages
) {

    public PagingResponseDTO(List<T> content, boolean empty, long number, long totalPages) {
        this.content = content;
        this.empty = empty;
        this.number = number;
        this.totalPages = totalPages;
    }

    public PagingResponseDTO(Page<T> pageableResponse) {
        this(
                pageableResponse.getContent(),
                pageableResponse.isEmpty(),
                pageableResponse.getNumber(),
                pageableResponse.getTotalPages()
        );
    }

    public PagingResponseDTO(PagingListDTO<T> dto) {
        this(
                dto.content(),
                dto.pagingData().isEmpty(),
                dto.pagingData().getNumber(),
                dto.pagingData().getTotalPages()
        );
    }
}
