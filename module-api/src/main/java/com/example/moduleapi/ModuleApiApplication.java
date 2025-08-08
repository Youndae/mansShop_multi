package com.example.moduleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.moduleapi",
                "com.example.modulecommon",
                "com.example.moduleconfig",
                "com.example.moduleadmin",
                "com.example.moduleauth",
                "com.example.moduleauthapi",
                "com.example.modulecart",
                "com.example.modulecache",
                "com.example.modulefile",
                "com.example.modulemypage",
                "com.example.modulenotification",
                "com.example.moduleorder",
                "com.example.moduleproduct",
                "com.example.moduleuser",
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleApiApplication.class, args);
    }

}
