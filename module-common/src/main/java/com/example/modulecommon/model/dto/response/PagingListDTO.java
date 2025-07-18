package com.example.modulecommon.model.dto.response;

import java.util.List;

public record PagingListDTO <T>(
        List<T> content,
        PagingMappingDTO pagingData
) {
}
