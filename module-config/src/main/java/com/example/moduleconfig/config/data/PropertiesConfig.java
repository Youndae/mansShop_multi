package com.example.moduleconfig.config.data;

import com.example.moduleconfig.properties.*;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableConfigurationProperties({
        AwsCredentialsProperties.class,
        AwsS3Properties.class,
        CacheProperties.class,
        CookieProperties.class,
        DataSourceProperties.class,
        FallbackProperties.class,
        IamportProperties.class,
        JwtSecretProperties.class,
        MailProperties.class,
        OAuth2Properties.class,
        RabbitMQConnectionProperties.class,
        RabbitMQProperties.class,
        RedisProperties.class,
        TokenProperties.class,
        TokenRedisProperties.class
})
public class PropertiesConfig {

    @Bean(name = "filePath")
    @Profile({"dev", "test"})
    public PropertiesFactoryBean filePathPropertiesFactoryBean() {
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
