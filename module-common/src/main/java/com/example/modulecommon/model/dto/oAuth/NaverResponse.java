package com.example.modulecommon.model.dto.oAuth;

import com.example.modulecommon.model.enumuration.OAuthProvider;

import java.util.Map;

public record NaverResponse(
        Map<String, Object> attributes
) implements OAuth2Response{

    public NaverResponse(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProvider() {
        return OAuthProvider.NAVER.getKey();
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }
}
