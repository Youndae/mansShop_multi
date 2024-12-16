package com.example.moduleconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class ModuleConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleConfigApplication.class, args);
    }

}
