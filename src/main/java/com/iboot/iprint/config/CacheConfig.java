package com.iboot.iprint.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * API Key 验证缓存
     * Key = api_key 字符串, Value = 是否有效
     * 10分钟过期自动从数据库刷新，最大1000条
     */
    @Bean
    public Cache<String, Boolean> apiKeyCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }
}
