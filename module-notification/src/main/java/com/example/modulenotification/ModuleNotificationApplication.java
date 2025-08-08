package com.example.modulenotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.modulenotification",
                "com.example.modulecommon",
                "com.example.moduleconfig",
                "com.example.moduleuser",
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleNotificationApplication.class, args);
    }

}
