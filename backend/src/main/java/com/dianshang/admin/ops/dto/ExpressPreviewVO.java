package com.dianshang.admin.ops.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExpressPreviewVO {

    private String orderId;
    private String expressCompany;
    private String expressNumber;
    private String templateName;
    private String templateSpec;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private String senderName;
    private String senderPhone;
    private String senderAddress;

    private List<ExpressSkuItemVO> skuList;
}
