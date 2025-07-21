package com.example.moduleapi.mapper;

import com.example.moduleapi.model.response.PagingElementsResponseDTO;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PagingResponseMapper {

    public <T> ResponseEntity<PagingResponseDTO<T>> toPagingResponse(PagingListDTO<T> content) {
        PagingResponseDTO<T> responseDTO = new PagingResponseDTO<>(content);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    public <T> ResponseEntity<PagingResponseDTO<T>> toPagingResponse(Page<T> content) {
        PagingResponseDTO<T> responseDTO = new PagingResponseDTO<>(content);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    public <T> ResponseEntity<PagingElementsResponseDTO<T>> toPagingElementsResponse(PagingListDTO<T> content) {
        PagingElementsResponseDTO<T> responseDTO = new PagingElementsResponseDTO<>(content);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    public <T> ResponseEntity<PagingElementsResponseDTO<T>> toPagingElementsResponse(Page<T> content) {
        PagingElementsResponseDTO<T> responseDTO = new PagingElementsResponseDTO<>(content);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }
}
