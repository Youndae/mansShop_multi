package com.example.modulemypage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ModuleMypageApplication.class)
@ComponentScan(basePackages = {"com.example.modulecommon", "com.example.moduleconfig"})
@ActiveProfiles("test")
class ModuleMypageApplicationTests {

    @Test
    void contextLoads() {
    }

}
