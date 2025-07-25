package com.example.moduleproduct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.moduleproduct",
                "com.example.moduleuser"
        }
)
public class ModuleProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleProductApplication.class, args);
    }

}
