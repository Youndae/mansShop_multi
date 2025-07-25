package com.example.moduleconfig.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "fallback")
@Getter
@Setter
@ToString
public class FallbackProperties {
    private Map<String, Redis> redis;

    @Getter
    @Setter
    @ToString
    public static class Redis {
        private String prefix;
    }
}
