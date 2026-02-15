package com.iboot.iprint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_print_template")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintTemplate extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "template_data", columnDefinition = "text")
    private String templateData;

    @Column(name = "print_data", columnDefinition = "text")
    private String printData;
}
