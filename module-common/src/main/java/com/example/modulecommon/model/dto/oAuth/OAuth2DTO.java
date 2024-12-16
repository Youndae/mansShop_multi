package com.example.modulecommon.model.dto.oAuth;

import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;

import java.util.List;

public record OAuth2DTO(
        String userId,
        String username,
        List<Auth> authList,
        String nickname
) {

    public OAuth2DTO(Member existsData) {
        this(
                existsData.getUserId(),
                existsData.getUserName(),
                existsData.getAuths(),
                existsData.getNickname()
        );
    }
}
