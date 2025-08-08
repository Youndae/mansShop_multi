package com.example.modulecart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.modulecart",
                "com.example.modulecommon",
                "com.example.moduleconfig",
                "com.example.moduleuser",
                "com.example.moduleproduct"
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleCartApplication.class, args);
    }

}
