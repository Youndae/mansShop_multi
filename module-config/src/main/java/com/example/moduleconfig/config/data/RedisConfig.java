package com.example.moduleconfig.config.data;

import com.example.moduleconfig.properties.RedisProperties;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.TimeoutOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    private final RedisProperties redisProperties;

    private final Environment environment;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setHostName(redisProperties.getHost());
        standaloneConfiguration.setPort(redisProperties.getPort());

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .clientOptions(
                        ClientOptions.builder()
                                .timeoutOptions(TimeoutOptions.enabled())
                                .build()
                );

        if(environment.acceptsProfiles(Profiles.of("prod"))) {
            log.info("Redis SSL is enabled in the 'prod' profile");
            builder.useSsl();
        }else {
            log.info("Redis SSL is disabled in the 'dev' profile");
        }

        return new LettuceConnectionFactory(standaloneConfiguration, builder.build());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);

        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringRedisTemplate.setValueSerializer(new StringRedisSerializer());
        stringRedisTemplate.setConnectionFactory(factory);

        return stringRedisTemplate;
    }

    @Bean
    public RedisTemplate<String, Long> longRedisTemplate(LettuceConnectionFactory factory) {

        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));

        return template;
    }
}
