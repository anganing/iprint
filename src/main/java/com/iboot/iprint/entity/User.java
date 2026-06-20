package com.iboot.iprint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("sys_user")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    private String username;

    private String password;
}
