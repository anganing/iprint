package com.iboot.iprint.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("sys_print_template")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintTemplate extends BaseEntity {

    private String code;

    private String name;

    @TableField("template_data")
    private String templateData;

    @TableField("print_data")
    private String printData;
}
