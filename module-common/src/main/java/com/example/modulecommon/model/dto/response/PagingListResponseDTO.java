package com.example.modulecommon.model.dto.response;


import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;


//@Schema(name = "pagingListResponseDTO Schema", description = "response pagination", contentMediaType = "application/json")
public record PagingListResponseDTO<T>(
//        @ArraySchema(schema = @Schema(description = "content items", name = "content"))
        List<T> content,
//        @Schema(description = "pagination metadata")
        PagingMappingDTO pagingData
) {
}
