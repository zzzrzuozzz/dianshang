package com.dianshang.admin.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ums_member_tag")
public class MemberTagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_code", nullable = false, unique = true, length = 20)
    private String tagCode;

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    @Column(name = "rule_json", columnDefinition = "CLOB")
    private String ruleJson;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0;

    @Column(nullable = false)
    private Boolean deleted = false;
}
