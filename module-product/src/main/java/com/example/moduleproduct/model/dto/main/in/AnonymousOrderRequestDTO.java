package com.example.moduleproduct.model.dto.main.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "비회원 주문 조회 데이터")
public record AnonymousOrderRequestDTO(
        @Schema(name = "recipient", description = "수령인")
        @NotBlank(message = "수령인은 필수 데이터입니다.")
        String recipient,

        @Schema(name = "phone", description = "수령인 연락처")
        @NotBlank(message = "연락처는 필수 데이터입니다.")
        @Pattern(
                regexp = "^[0-9]{10,11}$",
                message = "전화번호는 - 를 제외한 숫자 10 ~ 11자리여야 합니다."
        )
        String phone,

        @Schema(name = "page", description = "조회 페이지 번호.")
        @Min(value = 1, message = "Integrity Page")
        int page
) {
}
