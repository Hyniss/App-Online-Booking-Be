package com.fpt.h2s.configurations;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Getter
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {
    
    private String redisPort;
    
    private String redisHost;
    
    private String redisPassword;
    
    private String redisTokenSecret;
    
    private final ConsulConfiguration consul;
    
    @PostConstruct
    private void postInit() {
        this.redisPort = this.consul.get("service.redis.PORT");
        this.redisHost = this.consul.get("service.redis.HOST");
        this.redisPassword = this.consul.get("service.redis.PASSWORD");
        this.redisTokenSecret = this.consul.get("service.redis.TOKEN_SECRET");
    }
    
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        final RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setPort(Integer.parseInt(this.redisPort));
        redisConfig.setHostName(this.redisHost);
        redisConfig.setPassword(this.redisPassword);
        return new JedisConnectionFactory(redisConfig);
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}