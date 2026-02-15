package com.iboot.iprint.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {
    private Long id;
    private String username;
}
