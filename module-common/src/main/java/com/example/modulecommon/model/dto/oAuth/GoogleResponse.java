package com.example.modulecommon.model.dto.oAuth;

import com.example.modulecommon.model.enumuration.OAuthProvider;

import java.util.Map;

public record GoogleResponse(
        Map<String, Object> attribute
) implements OAuth2Response{

    @Override
    public String getProvider() {
        return OAuthProvider.GOOGLE.getKey();
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }
}
