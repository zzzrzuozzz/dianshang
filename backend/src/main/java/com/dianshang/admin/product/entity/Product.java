package com.dianshang.admin.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "pms_product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_no", nullable = false, unique = true, length = 20)
    private String productNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 200)
    private String subtitle;

    @Column(length = 500)
    private String thumb;

    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice = BigDecimal.ZERO;

    @Column(name = "discount_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountPrice = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    private String status = "off";

    @Column(name = "audit_status", nullable = false, length = 20)
    private String auditStatus = "pending";

    @Column(length = 500)
    private String remark;

    @Column(length = 50)
    private String sku;

    @Column(name = "sort_num", nullable = false)
    private Integer sortNum = 0;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "month_sales", nullable = false)
    private Integer monthSales = 0;

    @Column(name = "total_sales", nullable = false)
    private Integer totalSales = 0;

    @Column(length = 50)
    private String supplier;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Column(name = "brand_code", length = 50)
    private String brandCode;

    @Column(columnDefinition = "CLOB")
    private String intro;

    @Column(name = "shipping_template", length = 20)
    private String shippingTemplate;

    @Column(name = "stock_warning")
    private Integer stockWarning = 0;

    @Column(length = 20)
    private String unit;

    @Column(length = 20)
    private String weight;

    @Column(name = "pre_sale")
    private Boolean preSale = false;

    @Column(name = "recommend_tags", length = 200)
    private String recommendTags;

    @Column(name = "service_tags", length = 200)
    private String serviceTags;

    @Column(name = "product_tags", length = 200)
    private String productTags;

    @Column(name = "main_images", columnDefinition = "CLOB")
    private String mainImages;

    @Column(name = "white_image", length = 500)
    private String whiteImage;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "detail_content", columnDefinition = "CLOB")
    private String detailContent;

    /** JSON: [["省code","市code","区code"],...] 配送可售区域，空表示全国 */
    @Column(name = "delivery_regions", columnDefinition = "CLOB")
    private String deliveryRegions;

    @Column(nullable = false)
    private Boolean deleted = false;
}
