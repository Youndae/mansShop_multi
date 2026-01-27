package com.example.moduleorder.model.dto.out;

import com.example.moduleorder.model.dto.business.OrderDataDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderDataResponseDTO(
        @NotEmpty(message = "Integrity orderData")
        List<OrderDataDTO> orderData,

        @Min(value = 0, message = "Integrity totalPrice")
        int totalPrice
) {
}
