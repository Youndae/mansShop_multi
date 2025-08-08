package com.example.modulecache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.example.modulecache",
                "com.example.moduleconfig",
                "com.example.modulecommon",
                "com.example.moduleproduct",
                "com.example.moduleorder",
                "com.example.modulemypage",
        }
)
@EntityScan(basePackages = "com.example.modulecommon.model")
public class ModuleCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleCacheApplication.class, args);
    }

}
