package com.example.moduletest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = "com.example"
)
public class ModuleTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleTestApplication.class, args);
    }

}
