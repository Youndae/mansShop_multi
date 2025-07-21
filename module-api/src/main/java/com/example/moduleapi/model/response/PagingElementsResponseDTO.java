package com.example.moduleapi.model.response;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public record PagingElementsResponseDTO <T> (
        List<T> content,
        boolean empty,
        long number,
        long totalPages,
        long totalElements
) {

    public PagingElementsResponseDTO(Page<T> pageableResponse) {
        this(
                pageableResponse.getContent(),
                pageableResponse.isEmpty(),
                pageableResponse.getNumber(),
                pageableResponse.getTotalPages(),
                pageableResponse.getTotalElements()
        );
    }

    public PagingElementsResponseDTO(PagingListDTO<T> dto) {
        this(
                dto.content(),
                dto.pagingData().isEmpty(),
                dto.pagingData().getNumber(),
                dto.pagingData().getTotalPages(),
                dto.pagingData().getTotalElements()
        );
    }
}
