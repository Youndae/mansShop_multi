package com.example.moduleconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Getter
@Setter
public class OAuth2Properties {
    private Map<String, Provider> provider;
    private Registration registration;

    @Getter
    @Setter
    public static class Provider {
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
        private String userNameAttribute;
    }

    @Getter
    @Setter
    public static class Registration {
        private Client google;
        private Client naver;
        private Client kakao;
    }

    @Getter
    @Setter
    public static class Client {
        private String clientName;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizationGrantType;
        private String clientAuthenticationMethod;
        private String[] scope;
    }
}
