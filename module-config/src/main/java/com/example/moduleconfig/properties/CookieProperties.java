package com.example.moduleconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cookie")
@Getter
@Setter
public class CookieProperties {

    private Ino ino = new Ino();
    private Cart cart = new Cart();

    @Getter
    @Setter
    public static class Ino {
        private String header;
        private int age;
    }

    @Getter
    @Setter
    public static class Cart {
        private String header;
        private int age;
        private String uid;
    }
}
