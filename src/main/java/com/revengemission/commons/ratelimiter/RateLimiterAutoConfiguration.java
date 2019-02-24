package com.revengemission.commons.ratelimiter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

@Configuration
@EnableConfigurationProperties(RateLimiterProperties.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RateLimiterAutoConfiguration {

    @Autowired
    private RateLimiterProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
        redisStandaloneConfiguration.setPassword(properties.getPassword());
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean(name = "intRedisTemplate")
    public RedisTemplate<String, Serializable> intRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    RateLimiterAop rateLimiterAop(RedisTemplate<String, Serializable> intRedisTemplate) {
        return new RateLimiterAop(intRedisTemplate);
    }

}

