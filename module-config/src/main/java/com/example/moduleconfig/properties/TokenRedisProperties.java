package com.example.moduleconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token-redis")
@Getter
@Setter
public class TokenRedisProperties {

    private Access access = new Access();

    private Refresh refresh = new Refresh();

    private Temporary temporary = new Temporary();

    @Getter
    @Setter
    public static class Access {
        private int expiration;
        private String prefix;
    }

    @Getter
    @Setter
    public static class Refresh {
        private int expiration;
        private String prefix;
    }

    @Getter
    @Setter
    public static class Temporary {
        private int expiration;
    }
}
