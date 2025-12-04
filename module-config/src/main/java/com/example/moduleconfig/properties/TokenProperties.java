package com.example.moduleconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token")
@Getter
@Setter
public class TokenProperties {

    private String prefix;
    private Access access = new Access();
    private Refresh refresh = new Refresh();
    private Temporary temporary = new Temporary();
    private Order order = new Order();

    @Getter
    @Setter
    public static class Access {
        private String header;
        private long expiration;
    }

    @Getter
    @Setter
    public static class Refresh {
        private String header;
        private long expiration;
    }

    @Getter
    @Setter
    public static class Temporary {
        private String header;
        private long expiration;
    }

    @Getter
    @Setter
    public static class Order {
        private String header; // order
        private long expiration; // minute 10
    }
}
