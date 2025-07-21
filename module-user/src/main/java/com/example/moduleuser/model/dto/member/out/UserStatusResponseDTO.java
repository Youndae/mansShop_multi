package com.example.moduleuser.model.dto.member.out;

import com.example.moduleauth.config.user.CustomUser;
import com.example.modulecommon.model.enumuration.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class UserStatusResponseDTO {

    private final String userId;

    private final String role;

    public UserStatusResponseDTO(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        this.userId = authentication.getName();
        this.role = Role.getHighestRole(authorities);
    }

    public UserStatusResponseDTO(CustomUser customUser) {
        Collection<? extends GrantedAuthority> authorities = customUser.getAuthorities();

        this.userId = customUser.getUserId();
        this.role = Role.getHighestRole(authorities);
    }
}
