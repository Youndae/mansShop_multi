package com.example.modulemypage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.modulemypage",
                "com.example.moduleorder"
        }
)
public class ModuleMypageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleMypageApplication.class, args);
    }

}
