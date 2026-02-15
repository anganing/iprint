package com.iboot.iprint.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiKeyStatus {
    DISABLED(0, "禁用"),
    ACTIVE(1, "启用");

    private final int code;
    private final String description;
}
