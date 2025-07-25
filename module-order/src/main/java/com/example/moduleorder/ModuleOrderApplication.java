package com.example.moduleorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.moduleorder",
                "com.example.modulecart",
                "com.example.moduleconfig",
                "com.example.moduleproduct"
        }
)
public class ModuleOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleOrderApplication.class, args);
    }

}
