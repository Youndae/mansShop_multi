package com.example.moduleorder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ModuleOrderApplication.class)
@ComponentScan(basePackages = {"com.example.modulecommon", "com.example.moduleconfig"})
@ActiveProfiles("test")
class ModuleOrderApplicationTests {

    @Test
    void contextLoads() {
    }

}
