package com.dianshang.admin.product.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.common.TreeNodeVO;
import com.dianshang.admin.product.dto.*;
import com.dianshang.admin.product.service.BrandService;
import com.dianshang.admin.product.service.CategoryService;
import com.dianshang.admin.product.service.CommentService;
import com.dianshang.admin.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final CommentService commentService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             BrandService brandService,
                             CommentService commentService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.commentService = commentService;
    }

    @GetMapping("/{productNo}")
    public ApiResponse<ProductDetailVO> detail(@PathVariable String productNo) {
        return ApiResponse.ok(productService.getDetail(productNo));
    }

    @PostMapping
    public ApiResponse<ProductSaveResultVO> create(@Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @PutMapping("/{productNo}")
    public ApiResponse<ProductSaveResultVO> update(@PathVariable String productNo,
                                                   @Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.ok(productService.update(productNo, request));
    }

    @GetMapping("/list")
    public ApiResponse<ProductPageVO> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(productService.list(keyword, category, status, page, pageSize));
    }

    @GetMapping("/audit/list")
    public ApiResponse<ProductPageVO> auditList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "all") String auditStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(productService.auditList(keyword, category, auditStatus, page, pageSize));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable String id, @Valid @RequestBody StatusRequest request) {
        productService.updateStatus(id, request.getStatus());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/on")
    public ApiResponse<Void> batchOn(@Valid @RequestBody IdsRequest request) {
        productService.batchOn(request.getIds());
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/off")
    public ApiResponse<Void> batchOff(@Valid @RequestBody IdsRequest request) {
        productService.batchOff(request.getIds());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody IdsRequest request) {
        productService.batchDelete(request.getIds());
        return ApiResponse.ok(null);
    }

    @PostMapping("/audit/{id}")
    public ApiResponse<Void> audit(@PathVariable String id, @RequestBody AuditRequest request) {
        productService.audit(id, request.getPassed(), request.getRemark());
        return ApiResponse.ok(null);
    }

    @PostMapping("/audit/batch")
    public ApiResponse<Void> batchAudit(@Valid @RequestBody BatchAuditRequest request) {
        productService.batchAudit(request.getIds(), request.getPassed());
        return ApiResponse.ok(null);
    }

    @GetMapping("/category/tree")
    public ApiResponse<List<TreeNodeVO>> categoryTree() {
        return ApiResponse.ok(categoryService.tree());
    }

    @GetMapping("/category/options")
    public ApiResponse<List<CategoryOptionVO>> categoryOptions() {
        return ApiResponse.ok(categoryService.options());
    }

    @GetMapping("/category/list")
    public ApiResponse<PageResult<CategoryVO>> categoryList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(categoryService.list(keyword, level, page, pageSize));
    }

    @PostMapping("/category")
    public ApiResponse<CategoryVO> createCategory(@RequestBody CategoryVO request) {
        return ApiResponse.ok(categoryService.save(request));
    }

    @PutMapping("/category/{id}")
    public ApiResponse<CategoryVO> updateCategory(@PathVariable String id, @RequestBody CategoryVO request) {
        return ApiResponse.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/category/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable String id) {
        categoryService.delete(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/category/{id}/visible")
    public ApiResponse<Void> categoryVisible(@PathVariable String id, @RequestBody VisibleRequest request) {
        categoryService.updateVisible(id, Boolean.TRUE.equals(request.getVisible()));
        return ApiResponse.ok(null);
    }

    @GetMapping("/brand/list")
    public ApiResponse<PageResult<BrandVO>> brandList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(brandService.list(keyword, status, page, pageSize));
    }

    @PostMapping("/brand")
    public ApiResponse<BrandVO> createBrand(@RequestBody BrandVO request) {
        return ApiResponse.ok(brandService.save(request));
    }

    @PutMapping("/brand/{id}")
    public ApiResponse<BrandVO> updateBrand(@PathVariable String id, @RequestBody BrandVO request) {
        return ApiResponse.ok(brandService.update(id, request));
    }

    @DeleteMapping("/brand/{id}")
    public ApiResponse<Void> deleteBrand(@PathVariable String id) {
        brandService.delete(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/brand/{id}/visible")
    public ApiResponse<Void> brandVisible(@PathVariable String id, @RequestBody VisibleRequest request) {
        brandService.updateVisible(id, Boolean.TRUE.equals(request.getVisible()));
        return ApiResponse.ok(null);
    }

    @GetMapping("/comment/overview")
    public ApiResponse<CommentOverviewVO> commentOverview() {
        return ApiResponse.ok(commentService.overview());
    }

    @GetMapping("/comment/list")
    public ApiResponse<CommentPageVO> commentList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String rating,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(commentService.list(keyword, rating, page, pageSize));
    }

    @PostMapping("/comment/{productNo}/feature")
    public ApiResponse<Void> featureComment(@PathVariable String productNo) {
        commentService.featureByProductNo(productNo);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/comment/{productNo}")
    public ApiResponse<Void> deleteComment(@PathVariable String productNo) {
        commentService.deleteByProductNo(productNo);
        return ApiResponse.ok(null);
    }
}
