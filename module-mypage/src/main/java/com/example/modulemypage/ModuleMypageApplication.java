package com.example.modulemypage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.modulemypage",
                "com.example.modulecommon",
                "com.example.moduleconfig",
                "com.example.moduleuser"
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleMypageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleMypageApplication.class, args);
    }

}
