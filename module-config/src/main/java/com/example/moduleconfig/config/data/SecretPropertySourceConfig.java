package com.example.moduleconfig.config.data;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.util.Map;

/**
 * AWS Secrets Manager PropertySource Config
 */
@Configuration
@Profile("prod")
public class SecretPropertySourceConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    // AWS Secrets Manager SecretID
    private final String secretId = "";

    @Bean
    public PropertySource<?> secretPropertySource() {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder
                                        .standard()
                                        .withRegion(region)
                                        .build();
        GetSecretValueRequest request = new GetSecretValueRequest()
                .withSecretId(secretId);
        GetSecretValueResult result = client.getSecretValue(request);

        String secretJSON = result.getSecretString();

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> jsonMap;

        try {
            jsonMap = om.readValue(secretJSON, new TypeReference<Map<String, Object>>() {});
        }catch (IOException e) {
            throw new IllegalArgumentException("Could not parse secret json: " + secretJSON);
        }

        return new MapPropertySource("awsSecret", jsonMap);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
            ConfigurableEnvironment env,
            @Lazy PropertySource<?> awsSecret
    ) {
        env.getPropertySources().addFirst(awsSecret);

        return new PropertySourcesPlaceholderConfigurer();
    }
}
