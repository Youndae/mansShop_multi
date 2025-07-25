package com.example.moduleorder.model.dto.business;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;

import java.time.LocalDateTime;

public record FailedOrderDTO(
        PaymentDTO paymentDTO,
        CartMemberDTO cartMemberDTO,
        LocalDateTime failedTime,
        String message
) {
}

