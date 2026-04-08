package com.example.modulecommon.model.enumuration;

import com.example.modulecommon.model.entity.Auth;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Role {

    MEMBER("ROLE_MEMBER", "member"),
    ADMIN("ROLE_ADMIN", "admin"),
    MANAGER("ROLE_MANAGER", "manager"),
    ANONYMOUS("", "Anonymous");

    private final String key;

    private final String role;

    public static String getHighestRole(Collection<? extends GrantedAuthority> authorities) {

        return getHighestRoleResult(authorities.size());
    }

    public static String getHighestRole(List<Auth> auths) {

        return getHighestRoleResult(auths.size());
    }

    private static String getHighestRoleResult(int size) {
        return switch (size) {
            case 3 -> ADMIN.role;
            case 2 -> MANAGER.role;
            default -> MEMBER.role;
        };
    }
}
