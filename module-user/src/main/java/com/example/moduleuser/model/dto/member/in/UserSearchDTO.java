package com.example.moduleuser.model.dto.member.in;

import com.example.modulecommon.utils.PhoneNumberUtils;

public record UserSearchDTO(
        String userName,
        String userPhone,
        String userEmail
) {

    public UserSearchDTO(String userName,
                         String userPhone,
                         String userEmail) {

        this.userName = userName;
        this.userPhone = PhoneNumberUtils.format(userPhone);
        this.userEmail = userEmail;
    }
}
