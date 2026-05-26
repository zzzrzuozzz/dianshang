package com.dianshang.admin.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ums_member_tag_rel")
public class MemberTagRelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_code", nullable = false, length = 20)
    private String tagCode;

    @Column(name = "user_no", nullable = false, length = 20)
    private String userNo;
}
