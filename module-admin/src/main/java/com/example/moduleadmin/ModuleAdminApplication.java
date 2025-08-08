package com.example.moduleadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.moduleadmin",
                "com.example.modulecommon",
                "com.example.moduleconfig",
                "com.example.moduleorder",
                "com.example.moduleproduct"
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleAdminApplication.class, args);
    }

}
