package com.example.modulecommon.model.dto.response;

import java.util.List;

public record PagingListResponseDTO<T>(
        List<T> content,
        PagingMappingDTO pagingData
) {
}
