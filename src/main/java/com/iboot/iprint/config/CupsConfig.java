package com.iboot.iprint.config;

import org.cups4j.CupsAuthentication;
import org.cups4j.CupsClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * CUPS 连接配置
 */
@Configuration
public class CupsConfig {

    private final CupsProperties cupsProperties;

    public CupsConfig(CupsProperties cupsProperties) {
        this.cupsProperties = cupsProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "iprint.cups", name = "enabled", havingValue = "true")
    public CupsClient cupsClient() {
        String scheme = cupsProperties.isSecure() ? "https" : "http";
        URI cupsUri = URI.create(scheme + "://" + cupsProperties.getHost() + ":" + cupsProperties.getPort());

        CupsAuthentication creds = null;
        if (StringUtils.hasText(cupsProperties.getUsername()) && StringUtils.hasText(cupsProperties.getPassword())) {
            creds = new CupsAuthentication(cupsProperties.getUsername(), cupsProperties.getPassword());
        }

        return new CupsClient(cupsUri, cupsProperties.getUsername(), creds);
    }
}
