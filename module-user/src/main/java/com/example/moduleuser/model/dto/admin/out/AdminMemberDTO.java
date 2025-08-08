package com.example.moduleuser.model.dto.admin.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminMemberDTO(
        String userId,
        String userName,
        String nickname,
        String phone,
        String email,
        LocalDate birth,
        long point,
        LocalDate createdAt
) {

    public AdminMemberDTO(String userId,
                          String userName,
                          String nickname,
                          String phone,
                          String email,
                          LocalDate birth,
                          long point,
                          LocalDateTime createdAt) {
        this(
                userId,
                userName,
                nickname,
                phone,
                email,
                birth,
                point,
                createdAt.toLocalDate()
        );
    }
}
