package com.example.moduleuser.model.dto.admin.in;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminMemberListRequestDTO(
        @Size(min = 2, message = "keyword Length is minimum 2")
        String keyword,
        @Min(value = 1, message = "page value is minimum 1")
        Integer page
) {
}
