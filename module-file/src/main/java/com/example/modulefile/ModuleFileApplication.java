package com.example.modulefile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.modulefile",
                "com.example.modulecommon",
                "com.example.moduleconfig"
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleFileApplication.class, args);
    }

}
