package com.iboot.iprint.config;

import com.iboot.iprint.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.http.server.PathContainer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Locale;

@Aspect
@Component
@RequiredArgsConstructor
public class DemoModeOperationAspect {

    private final DemoModeProperties demoModeProperties;
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void blockDemoModeOperation(JoinPoint joinPoint) {
        if (!demoModeProperties.isEnabled()) {
            return;
        }

        HttpServletRequest request = currentRequest();
        if (request == null) {
            return;
        }

        String requestMethod = request.getMethod();
        String requestPath = request.getRequestURI();
        PathContainer pathContainer = PathContainer.parsePath(requestPath);

        boolean blocked = demoModeProperties.getBlocked().stream()
                .anyMatch(operation -> matches(operation, requestMethod, pathContainer));
        if (blocked) {
            throw new BusinessException("演示模式禁止该操作");
        }
    }

    private boolean matches(DemoModeProperties.BlockedOperation operation, String requestMethod, PathContainer requestPath) {
        if (operation.getMethod() == null || operation.getPath() == null) {
            return false;
        }

        String method = operation.getMethod().toUpperCase(Locale.ROOT);
        if (!HttpMethod.valueOf(method).matches(requestMethod)) {
            return false;
        }

        return pathPatternParser.parse(operation.getPath()).matches(requestPath);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
