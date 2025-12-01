package com.example.moduleconfig.config.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class ExternalSystemHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisTemplate<String, String> redisTemplate;

    public ExternalSystemHealthIndicator(DataSource dataSource, RedisTemplate<String, String> redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        boolean dbUp = checkDatabase();
        boolean redisUp = checkRedis();

        if(dbUp && redisUp) {
            return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("redis", "UP")
                    .build();
        }else {
            return Health.down()
                    .withDetail("database", dbUp ? "UP" : "DOWN")
                    .withDetail("redis", redisUp ? "UP" : "DOWN")
                    .build();
        }
    }

    private boolean checkDatabase() {
        try(Connection conn = dataSource.getConnection()) {
            return conn.isValid(2);
        }catch (Exception e) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            return "PONG".equals(redisTemplate.getConnectionFactory().getConnection().ping());
        }catch(Exception e) {
            return false;
        }
    }
}
