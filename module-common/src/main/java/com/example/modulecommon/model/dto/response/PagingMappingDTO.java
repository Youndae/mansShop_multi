package com.example.modulecommon.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagingMappingDTO {
    private Long totalElements;

    private boolean empty;

    private long number;

    private long totalPages;


    public PagingMappingDTO(Long totalElements, int page, int amount) {
        long totalPages = 0;

        if(totalElements != null){
            totalPages = totalElements / amount;

            if(totalElements % amount != 0)
                totalPages += 1;
        }

        this.totalElements = totalElements;
        this.empty = totalElements == null;
        this.number = page;
        this.totalPages = totalPages;
    }
}
