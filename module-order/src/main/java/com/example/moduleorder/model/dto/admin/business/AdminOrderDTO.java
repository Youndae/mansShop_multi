package com.example.moduleorder.model.dto.admin.business;

import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleorder.model.dto.admin.out.AdminOrderDetailDTO;
import com.example.moduleorder.model.dto.admin.out.AdminOrderResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderDTO(
        long orderId,
        String recipient,
        String userId,
        String phone,
        LocalDateTime createdAt,
        String address,
        String orderStatus
) {

    public AdminOrderDTO(long orderId,
                         String recipient,
                         String userId,
                         String phone,
                         LocalDateTime createdAt,
                         String address,
                         String orderStatus) {
        this.orderId = orderId;
        this.recipient = recipient;
        this.userId = userId.equals(Role.ANONYMOUS.getRole()) ? "비회원" : userId;
        this.phone = phone;
        this.createdAt = createdAt;
        this.address = address;
        this.orderStatus = orderStatus;
    }

    public AdminOrderResponseDTO toResponseDTO(List<AdminOrderDetailDTO> detailList){
        return AdminOrderResponseDTO.builder()
                .orderId(orderId)
                .recipient(recipient)
                .userId(userId)
                .phone(phone)
                .createdAt(createdAt)
                .address(address)
                .orderStatus(orderStatus)
                .detailList(detailList)
                .build();
    }
}