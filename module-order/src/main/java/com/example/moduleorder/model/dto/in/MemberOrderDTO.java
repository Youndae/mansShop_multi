package com.example.moduleorder.model.dto.in;

import com.example.modulecommon.model.enumuration.Role;
import com.example.modulecommon.utils.PhoneNumberUtils;
import com.example.moduleproduct.model.dto.main.in.AnonymousOrderRequestDTO;
import lombok.Builder;

public record MemberOrderDTO(
        String userId,
        String recipient,
        String phone
) {

    public static MemberOrderDTO fromAnonymousOrderRequestDTO(AnonymousOrderRequestDTO requestDTO) {
        return new MemberOrderDTO(
                Role.ANONYMOUS.getRole(),
                requestDTO.recipient(),
                requestDTO.phone()
        );
    }

    @Builder
    public MemberOrderDTO(String userId, String recipient, String phone) {
        this.userId = userId;
        this.recipient = recipient;
        this.phone = PhoneNumberUtils.format(phone);
    }
}
