package com.example.modulecommon.model.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PagingMappingDTO {
    private Long totalElements;

    private boolean empty;

    private long number;

    private long totalPages;


    public PagingMappingDTO(Long totalElements, int page, int amount) {
        this.totalElements = totalElements;
        this.empty = totalElements == 0L;
        this.number = page;
        this.totalPages = (int) Math.ceil((double) totalElements / amount);
    }

    public <T> PagingMappingDTO (Page<T> pageObject) {
        this.totalElements = pageObject.getTotalElements();
        this.empty = pageObject.isEmpty();
        this.number = pageObject.getNumber();
        this.totalPages = pageObject.getTotalPages();
    }
}
