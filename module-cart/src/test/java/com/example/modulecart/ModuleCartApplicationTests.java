package com.example.modulecart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ModuleCartApplication.class)
@ComponentScan(basePackages = {"com.example.modulecommon", "com.example.moduleconfig"})
@ActiveProfiles("test")
class ModuleCartApplicationTests {

    @Test
    void contextLoads() {
    }

}
