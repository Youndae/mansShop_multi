package com.example.moduleauth.config.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface CustomUserDetails {

    String getUserId();

    Collection<? extends GrantedAuthority> getAuthorities();
}
