package com.dianshang.admin.config;

import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.entity.ProductBrand;
import com.dianshang.admin.product.entity.ProductCategory;
import com.dianshang.admin.product.entity.ProductComment;
import com.dianshang.admin.product.repository.ProductBrandRepository;
import com.dianshang.admin.product.repository.ProductCategoryRepository;
import com.dianshang.admin.product.repository.ProductCommentRepository;
import com.dianshang.admin.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    private static final String THUMB =
            "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

    private final ProductCategoryRepository categoryRepository;
    private final ProductBrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductCommentRepository commentRepository;

    public ProductDataInitializer(ProductCategoryRepository categoryRepository,
                                  ProductBrandRepository brandRepository,
                                  ProductRepository productRepository,
                                  ProductCommentRepository commentRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        seedCategories();
        seedBrands();
        seedProducts();
        seedComments();
    }

    private void seedCategories() {
        ProductCategory daily = saveCategory("daily", "日用百货", 0L, 1, 100);
        saveCategory("daily-paper", "卫生纸", daily.getId(), 2, 0);
        saveCategory("daily-bag", "垃圾袋", daily.getId(), 2, 0);

        ProductCategory baby = saveCategory("baby", "母婴宠物", 0L, 1, 80);
        saveCategory("baby-milk", "牛奶", baby.getId(), 2, 0);
        saveCategory("baby-snack", "火腿肠", baby.getId(), 2, 0);

        ProductCategory brand = saveCategory("brand", "大牌", 0L, 1, 60);
        saveCategory("brand-tshirt", "T恤", brand.getId(), 2, 0);
        saveCategory("brand-women", "女装", brand.getId(), 2, 0);

        saveCategory("625543", "日用百货", 0L, 1, 100);
        saveCategory("625544", "母婴宠物", 0L, 1, 80);
        saveCategory("625545", "大牌", 0L, 1, 60);
        saveCategory("625546", "女装", 0L, 1, 45);
    }

    private ProductCategory saveCategory(String code, String name, Long parentId, int level, int count) {
        ProductCategory c = new ProductCategory();
        c.setCode(code);
        c.setName(name);
        c.setParentId(parentId);
        c.setLevelNum(level);
        c.setProductCount(count);
        c.setUnit("件");
        c.setVisible(!"625545".equals(code));
        c.setSortNum(1);
        return categoryRepository.save(c);
    }

    private void seedBrands() {
        saveBrand("B001", "华为", "H", 120, "自营", true, 1);
        saveBrand("B002", "小米", "X", 98, "第三方", true, 2);
        saveBrand("B003", "百事可乐", "B", 45, "自营", false, 3);
        saveBrand("B004", "耐克", "N", 67, "第三方", true, 4);
    }

    private void saveBrand(String code, String name, String initial, int count, String supplier,
                           boolean visible, int sort) {
        ProductBrand b = new ProductBrand();
        b.setCode(code);
        b.setName(name);
        b.setInitialChar(initial);
        b.setProductCount(count);
        b.setSupplier(supplier);
        b.setVisible(visible);
        b.setSortNum(sort);
        brandRepository.save(b);
    }

    private void seedProducts() {
        saveProduct("025342", "2024新款夏季纯棉T恤 男女同款宽松短袖", "舒适透气 多色可选",
                "on", "passed", "", "SKU-001", 1, 1200, 352, 2555, "自营", "daily", "B004");
        saveProduct("025343", "进口全脂纯牛奶 1L*12盒整箱装", "新西兰进口 高钙营养",
                "off", "pending", "", "SKU-002", 2, 800, 128, 980, "第三方", "baby", "B002");
        saveProduct("025344", "加厚垃圾袋 家用厨房一次性", "50只装 不易破",
                "on", "rejected", "商品信息不全", "SKU-003", 3, 5000, 890, 12000, "自营", "daily", "B003");

        for (int i = 5; i <= 24; i++) {
            String no = String.format("0253%02d", i);
            saveProduct(no, "示例商品 " + no, "副标题 " + no,
                    i % 3 == 0 ? "off" : "on",
                    i % 5 == 0 ? "pending" : (i % 7 == 0 ? "rejected" : "passed"),
                    i % 7 == 0 ? "信息不全" : "",
                    "SKU-" + no, i, 100 * i, 10 * i, 50 * i,
                    i % 2 == 0 ? "自营" : "第三方",
                    i % 2 == 0 ? "daily" : "baby",
                    i % 2 == 0 ? "B001" : "B002");
        }
    }

    private void saveProduct(String no, String title, String subtitle, String status, String auditStatus,
                             String remark, String sku, int sort, int stock, int monthSales, int totalSales,
                             String supplier, String categoryCode, String brandCode) {
        Product p = new Product();
        p.setProductNo(no);
        p.setTitle(title);
        p.setSubtitle(subtitle);
        p.setThumb(THUMB);
        p.setOriginalPrice(BigDecimal.valueOf(50 + sort * 3L));
        p.setDiscountPrice(BigDecimal.valueOf(30 + sort * 2L));
        p.setStatus(status);
        p.setAuditStatus(auditStatus);
        p.setRemark(remark);
        p.setSku(sku);
        p.setSortNum(sort);
        p.setStock(stock);
        p.setMonthSales(monthSales);
        p.setTotalSales(totalSales);
        p.setSupplier(supplier);
        p.setCategoryCode(categoryCode);
        p.setBrandCode(brandCode);
        productRepository.save(p);
    }

    private void seedComments() {
        saveComment("025342", "good", "挺好的，不错，穿着很舒服");
        saveComment("025343", "neutral", "一般般，包装还可以");
        saveComment("025344", "bad", "质量不太好，容易破");
        saveComment("025342", "good", "回购多次了");
        saveComment("025343", "bad", "物流有点慢");
    }

    private void saveComment(String productNo, String rating, String content) {
        ProductComment c = new ProductComment();
        c.setProductNo(productNo);
        c.setRating(rating);
        c.setContent(content);
        commentRepository.save(c);
    }
}
