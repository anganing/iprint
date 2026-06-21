package com.iboot.iprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "iprint.cups")
public class CupsProperties {

    private boolean enabled = false;

    private String host = "localhost";

    private int port = 631;

    private boolean secure = false;

    private String username;

    private String password;

    private String defaultPrinter;

    private int connectTimeoutMs = 3000;

    private int readTimeoutMs = 10000;
}
