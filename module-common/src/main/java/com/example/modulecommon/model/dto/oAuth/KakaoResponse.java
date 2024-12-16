package com.example.modulecommon.model.dto.oAuth;

import com.example.modulecommon.model.enumuration.OAuthProvider;

import java.util.Map;

public record KakaoResponse(
        Map<String, Object> attribute
) implements OAuth2Response{

    private static Map<String, Object> account_attribute;

    private static Map<String, Object> profile_attribute;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        account_attribute = (Map<String, Object>) attribute.get("kakao_account");
        profile_attribute = (Map<String, Object>) account_attribute.get("profile");
    }

    @Override
    public String getProvider() {
        return OAuthProvider.KAKAO.getKey();
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return account_attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return profile_attribute.get("nickname").toString();
    }
}
