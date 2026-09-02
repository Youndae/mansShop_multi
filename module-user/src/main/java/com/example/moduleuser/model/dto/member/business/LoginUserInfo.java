package com.example.moduleuser.model.dto.member.business;

import com.example.modulecommon.model.enumuration.Role;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record LoginUserInfo(
        String userId,
        Role role
) {
    public LoginUserInfo(String userId, Collection<? extends GrantedAuthority> authorities) {
        this(
                userId,
                Role.fromKey(Role.getHighestRole(authorities))
        );
    }

    public LoginUserInfo(String userId, String highestRole) {
        this(
                userId,
                Role.fromKey(highestRole)
        );
    }
}
