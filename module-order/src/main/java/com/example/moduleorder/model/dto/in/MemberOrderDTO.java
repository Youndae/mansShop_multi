package com.example.moduleorder.model.dto.in;

import com.example.modulecommon.utils.PhoneNumberUtils;
import lombok.Builder;

public record MemberOrderDTO(
        String userId,
        String recipient,
        String phone
) {

    @Builder
    public MemberOrderDTO(String userId, String recipient, String phone) {
        this.userId = userId;
        this.recipient = recipient;
        this.phone = PhoneNumberUtils.format(phone);
    }
}
