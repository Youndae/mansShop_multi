package com.example.moduleauth.config.oauth;

import com.example.moduleauth.config.user.CustomUserDetails;
import com.example.modulecommon.model.dto.oAuth.OAuth2DTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomOAuth2User implements OAuth2User, CustomUserDetails {

    private final OAuth2DTO oAuth2DTO;

    public CustomOAuth2User(OAuth2DTO oAuth2DTO) {
        this.oAuth2DTO = oAuth2DTO;
    }

    @Override
    public String getUserId() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2DTO.authList()
                .stream()
                .map(auth -> (GrantedAuthority) auth::getAuth)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return oAuth2DTO.username();
    }
}
