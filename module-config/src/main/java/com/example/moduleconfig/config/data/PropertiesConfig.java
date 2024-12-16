package com.example.moduleconfig.config.data;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class PropertiesConfig {

    @Bean(name = "jwt")
    public PropertiesFactoryBean jwtPropertiesFactoryBean() throws Exception {
        String jwtPropertiesPath = "jwt.properties";

        return setPropertiesFactoryBean(jwtPropertiesPath);
    }

    @Bean(name = "filePath")
    public PropertiesFactoryBean filePathPropertiesFactoryBean() throws Exception {
        String filePathPropertiesPath = "filePath.properties";

        return setPropertiesFactoryBean(filePathPropertiesPath);
    }

    private PropertiesFactoryBean setPropertiesFactoryBean(String path) {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        ClassPathResource classPathResource = new ClassPathResource(path);

        propertiesFactoryBean.setLocation(classPathResource);

        return propertiesFactoryBean;
    }
}
