package com.example.moduleauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.moduleauth",
                "com.example.moduleauthapi",
                "com.example.moduleuser",
                "com.example.modulecommon",
                "com.example.moduleconfig",
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleAuthApplication.class, args);
    }

}
