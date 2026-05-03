package com.iboot.iprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "demo-mode")
public class DemoModeProperties {

    private boolean enabled;

    private List<BlockedOperation> blocked = new ArrayList<>();

    @Data
    public static class BlockedOperation {
        private String method;
        private String path;
    }
}
