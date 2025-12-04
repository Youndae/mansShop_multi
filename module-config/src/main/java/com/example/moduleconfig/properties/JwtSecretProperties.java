package com.example.moduleconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt.secret")
@Getter
@Setter
public class JwtSecretProperties {

    private String access;
    private String refresh;
    private String temporary;
}
