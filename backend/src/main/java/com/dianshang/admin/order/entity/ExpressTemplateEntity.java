package com.dianshang.admin.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "oms_express_template")
public class ExpressTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", nullable = false, unique = true, length = 20)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "express_company", nullable = false, length = 50)
    private String expressCompany;

    @Column(name = "template_spec", length = 50)
    private String templateSpec;

    @Column(length = 200)
    private String remark;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(name = "add_time", nullable = false)
    private LocalDateTime addTime = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean deleted = false;
}
