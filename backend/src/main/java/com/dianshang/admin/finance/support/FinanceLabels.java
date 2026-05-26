package com.dianshang.admin.finance.support;

import java.util.Map;

public final class FinanceLabels {

    private FinanceLabels() {
    }

    public static final Map<String, String> TRADE_TYPE = Map.of(
            "ORDER_IN", "订单收入",
            "REFUND_OUT", "退款支出",
            "WITHDRAW", "提现",
            "COMMISSION", "平台手续费"
    );

    public static final Map<String, String> PAYMENT_CHANNEL = Map.of(
            "WECHAT", "微信",
            "ALIPAY", "支付宝",
            "BALANCE", "余额"
    );

    public static final Map<Integer, String> TX_STATUS = Map.of(
            0, "入账失败",
            1, "记账成功",
            2, "对账异常"
    );

    public static final Map<Integer, String> WITHDRAW_STATUS = Map.of(
            0, "待审核",
            1, "转账中",
            2, "已驳回",
            3, "转账成功",
            4, "转账失败"
    );
}
