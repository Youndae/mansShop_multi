package com.example.moduleconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iamport")
@Getter
@Setter
public class IamportProperties {

    private String key;
    private String secret;
}
