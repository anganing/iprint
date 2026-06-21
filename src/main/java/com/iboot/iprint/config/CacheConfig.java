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

    /**
     * Hiprint 静态资源文本缓存
     * Key = 资源名称, Value = 资源内容
     */
    @Bean
    public Cache<String, String> hiprintStaticResourceCache() {
        return Caffeine.newBuilder()
                .maximumSize(16)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }
}
