package com.iboot.iprint.config;

import com.iboot.iprint.common.ApiKeyAuthFilter;
import com.iboot.iprint.common.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 暴露 AuthenticationManager，供 AuthController 手动调用登录认证
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离，禁用 CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // API Key 过滤器放在用户名密码认证之前
                // 这样带 X-API-Key 头的请求会优先通过 API Key 认证
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // URL 访问控制规则
                .authorizeHttpRequests(auth -> auth
                        // 登录接口公开
                        .requestMatchers("/api/auth/login").permitAll()
                        // 静态资源公开（Vue SPA 打包后放在 static 目录下）
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/assets/**"
                        ).permitAll()
                        // 所有 /api/** 接口需要认证（Session 或 API Key）
                        .requestMatchers("/api/**").authenticated()
                        // 其余请求放行（SPA 路由由前端处理）
                        .anyRequest().permitAll()
                )

                // Session 管理：同一用户最多一个会话
                .sessionManagement(session -> session
                        .maximumSessions(1)
                )

                // 自定义 JSON 格式的认证/授权失败响应（不重定向）
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        R.fail(401, "未登录或认证已过期")))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                                        R.fail(403, "访问被拒绝")))
                )

                // 注销配置：返回 JSON 而非重定向
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                writeJson(response, HttpServletResponse.SC_OK, R.ok()))
                );

        return http.build();
    }

    private void writeJson(HttpServletResponse response, int status, R<?> result) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
