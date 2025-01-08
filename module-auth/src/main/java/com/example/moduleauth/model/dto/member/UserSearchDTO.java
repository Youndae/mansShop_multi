package com.example.moduleauth.model.dto.member;

public record UserSearchDTO(
        String userName,
        String userPhone,
        String userEmail
) {

    public UserSearchDTO(String userName,
                         String userPhone,
                         String userEmail) {
        String phoneRegEx = "(\\d{3})(\\d{3,4})(\\d{4})";

        this.userName = userName;
        this.userPhone = userPhone == null ? null : userPhone.replaceAll(phoneRegEx, "$1-$2-$3");
        this.userEmail = userEmail;
    }
}
