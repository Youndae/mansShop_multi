package com.example.moduleconfig.config.data;

import com.example.moduleconfig.properties.RedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        log.info("=========================================redis=============================================");
        log.info("host:{}", redisProperties.getHost());
        log.info("port:{}", redisProperties.getPort());
        log.info("=========================================redis=============================================");

        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }

    private <V> RedisTemplate<String, V> buildTemplate(RedisSerializer<V> redisSerializer) {
        RedisTemplate<String, V> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(redisSerializer);
        template.setValueSerializer(redisSerializer);

        return template;
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        return buildTemplate(new GenericJackson2JsonRedisSerializer());
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringRedisTemplate.setValueSerializer(new StringRedisSerializer());
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory());

        return stringRedisTemplate;
    }

    @Bean
    public RedisTemplate<String, Long> longRedisTemplate() {
        return buildTemplate(new GenericToStringSerializer<>(Long.class));
    }
}
