package com.dianshang.admin.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "fin_withdraw_apply")
public class FinWithdrawApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apply_no", nullable = false, unique = true, length = 32)
    private String applyNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "apply_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal applyAmount;

    @Column(name = "actual_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "fee_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "bank_card_info", nullable = false, columnDefinition = "CLOB")
    private String bankCardInfo;

    @Column(name = "verify_status", nullable = false)
    private Integer verifyStatus = 0;

    @Column(name = "verify_user", length = 50)
    private String verifyUser;

    @Column(name = "verify_time")
    private LocalDateTime verifyTime;

    @Column(length = 255)
    private String remark;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}
