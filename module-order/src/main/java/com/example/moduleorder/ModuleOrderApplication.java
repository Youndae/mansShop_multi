package com.example.moduleorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.moduleorder",
                "com.example.modulecommon",
                "com.example.moduleconfig",
                "com.example.moduleuser",
                "com.example.modulecart",
                "com.example.moduleproduct"
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleOrderApplication.class, args);
    }

}
