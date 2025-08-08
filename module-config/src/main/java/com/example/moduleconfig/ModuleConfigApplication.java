package com.example.moduleconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.moduleconfig",
                "com.example.modulecommon"
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleConfigApplication.class, args);
    }

}
