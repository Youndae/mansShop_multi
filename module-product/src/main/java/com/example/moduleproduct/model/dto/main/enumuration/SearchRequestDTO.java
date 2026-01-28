package com.example.moduleproduct.model.dto.main.enumuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchRequestDTO(
        @NotBlank(message = "검색어는 필수 입력 사항입니다.")
        @Size(min = 2, message = "검색어는 2글자 이상 입력해야 합니다.")
        String keyword,

        @Min(value = 1, message = "Integrity page number")
        Integer page
) {
}
