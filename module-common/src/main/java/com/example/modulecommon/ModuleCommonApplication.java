package com.example.modulecommon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = "com.example.modulecommon"
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleCommonApplication.class, args);
    }

}
