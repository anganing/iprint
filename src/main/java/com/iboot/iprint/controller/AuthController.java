package com.iboot.iprint.controller;

import com.iboot.iprint.common.R;
import com.iboot.iprint.dto.ChangePasswordRequest;
import com.iboot.iprint.dto.LoginRequest;
import com.iboot.iprint.dto.UserInfoResponse;
import com.iboot.iprint.entity.User;
import com.iboot.iprint.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    // Spring Security 6+/7 不再自动将 SecurityContext 保存到 Session
    // 需要手动使用 SecurityContextRepository 保存
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public R<UserInfoResponse> login(@RequestBody @Valid LoginRequest request,
                                     HttpServletRequest httpRequest,
                                     HttpServletResponse httpResponse) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword())
        );

        // 创建新的 SecurityContext 并保存到 Session
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userService.getByUsername(request.getUsername());
        return R.ok(UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build());
    }

    @GetMapping("/info")
    public R<UserInfoResponse> info() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userService.getByUsername(username);
        return R.ok(UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build());
    }

    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        userService.changePassword(username, request);
        return R.ok();
    }

    // 注销由 Spring Security 的 LogoutFilter 处理，见 SecurityConfig
}
