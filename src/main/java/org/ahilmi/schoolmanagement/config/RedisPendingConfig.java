package org.ahilmi.schoolmanagement.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


@Configuration
public class RedisPendingConfig {

    @Bean
    public LettuceConnectionFactory pendingRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();

        config.setHostName("localhost");
        config.setPort(6379);
        config.setDatabase(2);

        return new LettuceConnectionFactory(config);
    }

    @Bean("pendingRedisTemplate")
    public StringRedisTemplate pendingRedisTemplate(@Qualifier("pendingRedisConnectionFactory") LettuceConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
