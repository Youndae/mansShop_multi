package com.example.moduletest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.moduletest",
                "com.example.modulecommon",
                "com.example.moduleconfig",
                "com.example.moduleauth",
                "com.example.moduleauthapi",
                "com.example.moduleapi",
                "com.example.moduleadmin",
                "com.example.moduleproduct",
                "com.example.moduleuser",
                "com.example.moduleorder",
                "com.example.modulecart",
                "com.example.modulecache",
                "com.example.modulefile",
                "com.example.modulemypage",
                "com.example.modulenotification",
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleTestApplication.class, args);
    }

}
