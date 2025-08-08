package com.example.moduleuser.model.dto.member.out;

import com.example.modulecommon.model.enumuration.Role;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class UserStatusResponseDTO {

    private final String userId;

    private final String role;

    @JsonCreator
    public UserStatusResponseDTO(@JsonProperty("userId") String userId,
                                 @JsonProperty("role") String role) {
        this.userId = userId;
        this.role = role;
    }

    public UserStatusResponseDTO(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        this.userId = authentication.getName();
        this.role = Role.getHighestRole(authorities);
    }

    public UserStatusResponseDTO(String userId, Collection<? extends GrantedAuthority> authorities) {

        this.userId = userId;
        this.role = Role.getHighestRole(authorities);
    }
}
