package com.iboot.iprint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 200)
    private String password;
}
