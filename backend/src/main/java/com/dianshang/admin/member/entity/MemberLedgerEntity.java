package com.dianshang.admin.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ums_member_ledger")
public class MemberLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ledger_no", nullable = false, unique = true, length = 30)
    private String ledgerNo;

    @Column(name = "user_no", nullable = false, length = 20)
    private String userNo;

    @Column(name = "ledger_type", nullable = false, length = 20)
    private String ledgerType;

    @Column(name = "change_type", nullable = false, length = 30)
    private String changeType;

    @Column(name = "before_qty", nullable = false)
    private Integer beforeQty = 0;

    @Column(name = "change_qty", nullable = false)
    private Integer changeQty = 0;

    @Column(name = "after_qty", nullable = false)
    private Integer afterQty = 0;

    @Column(length = 500)
    private String remark;

    @Column(name = "operator_name", length = 50)
    private String operatorName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
