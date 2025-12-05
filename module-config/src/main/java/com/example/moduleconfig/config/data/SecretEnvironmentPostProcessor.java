package com.example.moduleconfig.config.data;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class SecretEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String region = environment.getProperty("cloud.aws.region.static");

        if(region == null) {
            System.out.println("Cloud AWS region not set");
            return;
        }

        String secretId = "local-test-secret-id";

        try {
            AWSSecretsManager client = AWSSecretsManagerClientBuilder
                    .standard()
                    .withRegion(region)
                    .build();

            GetSecretValueRequest request = new GetSecretValueRequest()
                    .withSecretId(secretId);
            GetSecretValueResult result = client.getSecretValue(request);

            String secretJson = result.getSecretString();
            if(secretJson == null) {
                System.out.println("secretJson is null");
                return;
            }

            ObjectMapper om = new ObjectMapper();
            Map<String, Object> secretMap = om.readValue(secretJson, new TypeReference<Map<String, Object>>() {});

            Map<String, Object> flat = flatten("", secretMap);

            MapPropertySource propertySource = new MapPropertySource("awsSecret", flat);
            environment.getPropertySources().addFirst(propertySource);
        }catch (Exception e) {
            throw new IllegalArgumentException("Failed to load secret", e);
        }
    }

    private Map<String, Object> flatten(String prefix, Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();

        for(Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();

            Object value = entry.getValue();

            if(value instanceof Map<?, ?> nested) {
                result.putAll(flatten(key, (Map<String, Object>) nested));
            }else {
                result.put(key, value);
            }
        }

        return result;
    }
}
