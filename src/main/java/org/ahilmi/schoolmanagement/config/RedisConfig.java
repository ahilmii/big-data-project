
package org.ahilmi.schoolmanagement.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory(
            RedisProperties properties
    ) {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration();

        configuration.setHostName(properties.getHost());
        configuration.setPort(properties.getPort());
        configuration.setDatabase(properties.getDatabase());

        if (properties.getPassword() != null) {
            configuration.setPassword(properties.getPassword());
        }

        return new LettuceConnectionFactory(configuration);
    }
}
