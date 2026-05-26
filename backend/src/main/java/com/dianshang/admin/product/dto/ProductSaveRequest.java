package com.dianshang.admin.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSaveRequest {

    @NotBlank(message = "请选择商品分类")
    private String category;

    @NotBlank(message = "请输入商品名称")
    @Size(max = 60, message = "商品名称最多 60 字")
    private String name;

    @Size(max = 10, message = "副标题最多 10 字")
    private String subtitle;

    @NotBlank(message = "请选择品牌")
    private String brand;

    @NotBlank(message = "请输入商品介绍")
    @Size(max = 300, message = "商品介绍最多 300 字")
    private String intro;

    private String shipping;

    /** 配送区域：多选省市区代码路径，如 [["330000","330100","330106"]] */
    private List<List<String>> deliveryRegions;

    @Size(max = 30, message = "货号最多 30 字")
    private String sku;

    @NotNull(message = "请输入售价")
    @DecimalMin(value = "0", message = "售价不能为负数")
    private BigDecimal price;

    @DecimalMin(value = "0", message = "市场价不能为负数")
    private BigDecimal marketPrice;

    @NotNull(message = "请输入库存")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Min(value = 0, message = "库存预警值不能为负数")
    private Integer stockWarning;

    private String unit;
    private String weight;
    private Boolean preSale;
    private Boolean onSale;
    private List<String> recommend;
    private List<String> services;
    private String tags;
    private List<String> mainImages;
    private String whiteImage;
    private String video;
    private String detail;
}
