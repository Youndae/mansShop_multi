package com.example.moduleorder.model.dto.out;

import com.example.moduleorder.model.dto.business.OrderDataDTO;

import java.util.List;

public record OrderDataResponseDTO(
        List<OrderDataDTO> orderData,
        int totalPrice
) {
}
