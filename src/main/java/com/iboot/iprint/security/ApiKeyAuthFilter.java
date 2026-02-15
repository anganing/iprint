package com.iboot.iprint.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.iboot.iprint.enums.ApiKeyStatus;
import com.iboot.iprint.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API Key 认证过滤器
 * 检查请求头 X-API-Key，通过 Caffeine 缓存验证有效性
 * 缓存未命中时回查数据库，确保 API Key 的 CRUD 操作实时生效
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final Cache<String, Boolean> apiKeyCache;
    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        // 仅在存在 X-API-Key 头且尚未认证时处理
        if (apiKey != null && !apiKey.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            Boolean isValid = apiKeyCache.get(apiKey, key ->
                    apiKeyRepository.findByApiKey(key)
                            .map(k -> k.getStatus().equals(ApiKeyStatus.ACTIVE.getCode()))
                            .orElse(false)
            );

            if (Boolean.TRUE.equals(isValid)) {
                var auth = new UsernamePasswordAuthenticationToken(
                        "api-key-user", null,
                        List.of(new SimpleGrantedAuthority("ROLE_API_KEY"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("API Key 认证成功");
            } else {
                log.debug("API Key 认证失败: {}...",
                        apiKey.substring(0, Math.min(8, apiKey.length())));
            }
        }

        filterChain.doFilter(request, response);
    }
}
