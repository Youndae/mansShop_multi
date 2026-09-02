package com.example.modulecommon.model.enumuration;

import com.example.modulecommon.model.entity.Auth;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Role {

    MEMBER("ROLE_MEMBER", "member"),
    MANAGER("ROLE_MANAGER", "manager"),
    ADMIN("ROLE_ADMIN", "admin"),
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

    public static Role fromKey(String role) {
        return Arrays.stream(values())
                .filter(r -> r.getRole().equals(role))
                .findFirst()
                .orElse(ANONYMOUS);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch(this) {
            case ADMIN -> createAuthorities(ADMIN.key, MANAGER.key, MEMBER.key);
            case MANAGER -> createAuthorities(MANAGER.key, MEMBER.key);
            case MEMBER -> createAuthorities(MEMBER.key);
            default -> Collections.emptyList();
        };
    }

    private static List<GrantedAuthority> createAuthorities(String... keys) {
        return Arrays.stream(keys)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
