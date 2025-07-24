package com.example.modulecart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.modulecart",
                "com.example.moduleauth",
                "com.example.moduleproduct"
        }
)
public class ModuleCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleCartApplication.class, args);
    }

}
