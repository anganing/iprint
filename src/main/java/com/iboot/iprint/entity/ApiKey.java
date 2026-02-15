package com.iboot.iprint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_api_key")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    @Column(nullable = false)
    private Integer status;

    @Column(length = 500)
    private String description;
}
