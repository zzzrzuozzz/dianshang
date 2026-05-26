package com.dianshang.admin.product.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.TabCountVO;
import com.dianshang.admin.product.dto.*;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.repository.ProductRepository;
import com.dianshang.admin.product.support.ProductMapper;
import com.dianshang.admin.product.support.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductPageVO list(String keyword, String category, String status, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.ASC, "sortNum").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.forList(keyword, category, status), pageable);
        return buildPage(result, page, pageSize, true);
    }

    public ProductPageVO auditList(String keyword, String category, String auditStatus, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.ASC, "sortNum").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Product> result = productRepository.findAll(
                ProductSpecifications.forAudit(keyword, category, auditStatus), pageable);
        return buildPage(result, page, pageSize, false);
    }

    private ProductPageVO buildPage(Page<Product> result, int page, int pageSize, boolean listMode) {
        ProductPageVO vo = new ProductPageVO();
        vo.setList(result.getContent().stream().map(ProductMapper::toVO).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTabs(listMode ? buildListTabs() : buildAuditTabs());
        return vo;
    }

    private List<TabCountVO> buildListTabs() {
        long all = productRepository.countByDeletedFalse();
        return List.of(
                tab("all", "全部", all),
                tab("on", "已上架", productRepository.countByDeletedFalseAndStatus("on")),
                tab("off", "已下架", productRepository.countByDeletedFalseAndStatus("off")),
                tab("pending", "待审核", productRepository.countByDeletedFalseAndAuditStatus("pending")),
                tab("rejected", "未通过", productRepository.countByDeletedFalseAndAuditStatus("rejected"))
        );
    }

    private List<TabCountVO> buildAuditTabs() {
        long all = productRepository.countByDeletedFalse();
        long audited = productRepository.countByDeletedFalseAndAuditStatusIn(List.of("passed", "rejected"));
        return List.of(
                tab("all", "全部", all),
                tab("audited", "已审核", audited),
                tab("pending", "待审核", productRepository.countByDeletedFalseAndAuditStatus("pending")),
                tab("rejected", "未通过", productRepository.countByDeletedFalseAndAuditStatus("rejected"))
        );
    }

    private TabCountVO tab(String key, String label, long count) {
        return new TabCountVO(key, label, count);
    }

    @Transactional
    public void updateStatus(String productNo, String status) {
        Product product = requireProduct(productNo);
        if (!"on".equals(status) && !"off".equals(status)) {
            throw new BusinessException("无效的状态");
        }
        product.setStatus(status);
        productRepository.save(product);
    }

    @Transactional
    public void delete(String productNo) {
        Product product = requireProduct(productNo);
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Transactional
    public void batchOn(List<String> ids) {
        ids.forEach(id -> updateStatus(id, "on"));
    }

    @Transactional
    public void batchOff(List<String> ids) {
        ids.forEach(id -> updateStatus(id, "off"));
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        ids.forEach(this::delete);
    }

    @Transactional
    public void audit(String productNo, Boolean passed, String remark) {
        Product product = requireProduct(productNo);
        if (Boolean.TRUE.equals(passed)) {
            product.setAuditStatus("passed");
            product.setRemark("");
            product.setStatus("on");
        } else {
            product.setAuditStatus("rejected");
            product.setRemark(remark != null ? remark : "审核未通过");
        }
        productRepository.save(product);
    }

    @Transactional
    public void batchAudit(List<String> ids, Boolean passed) {
        String remark = Boolean.TRUE.equals(passed) ? "" : "批量审核未通过";
        ids.forEach(id -> audit(id, passed, remark));
    }

    private Product requireProduct(String productNo) {
        return productRepository.findByProductNoAndDeletedFalse(productNo)
                .orElseThrow(() -> new BusinessException("商品不存在"));
    }
}
