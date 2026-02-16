package com.iboot.iprint.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserInfoResponse {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
}
