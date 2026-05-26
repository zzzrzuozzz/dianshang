package com.dianshang.admin.product.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.product.dto.BrandVO;
import com.dianshang.admin.product.entity.ProductBrand;
import com.dianshang.admin.product.repository.ProductBrandRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class BrandService {

    private final ProductBrandRepository brandRepository;

    public BrandService(ProductBrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public PageResult<BrandVO> list(String keyword, String status, int page, int pageSize) {
        Specification<ProductBrand> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("code"), like)
                ));
            }
            if (StringUtils.hasText(status) && !"all".equals(status)) {
                if ("active".equals(status)) {
                    predicates.add(cb.isTrue(root.get("visible")));
                } else if ("disabled".equals(status)) {
                    predicates.add(cb.isFalse(root.get("visible")));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ProductBrand> result = brandRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by("sortNum").ascending()));
        List<BrandVO> list = result.getContent().stream().map(this::toVO).toList();
        return new PageResult<>(list, result.getTotalElements(), page, pageSize);
    }

    @Transactional
    public BrandVO save(BrandVO request) {
        ProductBrand entity = new ProductBrand();
        entity.setCode("B" + System.currentTimeMillis());
        entity.setName(request.getName());
        entity.setInitialChar(request.getInitial());
        entity.setProductCount(request.getCount() != null ? request.getCount() : 0);
        entity.setSupplier(request.getSupplier());
        entity.setVisible(request.getVisible() != null ? request.getVisible() : true);
        entity.setSortNum(request.getSort() != null ? request.getSort() : 0);
        return toVO(brandRepository.save(entity));
    }

    @Transactional
    public BrandVO update(String code, BrandVO request) {
        ProductBrand entity = requireByCode(code);
        if (StringUtils.hasText(request.getName())) {
            entity.setName(request.getName());
        }
        if (StringUtils.hasText(request.getInitial())) {
            entity.setInitialChar(request.getInitial());
        }
        if (request.getCount() != null) {
            entity.setProductCount(request.getCount());
        }
        if (request.getSupplier() != null) {
            entity.setSupplier(request.getSupplier());
        }
        if (request.getSort() != null) {
            entity.setSortNum(request.getSort());
        }
        return toVO(brandRepository.save(entity));
    }

    @Transactional
    public void delete(String code) {
        ProductBrand entity = requireByCode(code);
        entity.setDeleted(true);
        brandRepository.save(entity);
    }

    @Transactional
    public void updateVisible(String code, boolean visible) {
        ProductBrand entity = requireByCode(code);
        entity.setVisible(visible);
        brandRepository.save(entity);
    }

    private ProductBrand requireByCode(String code) {
        return brandRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("品牌不存在"));
    }

    private BrandVO toVO(ProductBrand b) {
        BrandVO vo = new BrandVO();
        vo.setId(b.getCode());
        vo.setName(b.getName());
        vo.setInitial(b.getInitialChar());
        vo.setCount(b.getProductCount());
        vo.setSupplier(b.getSupplier());
        vo.setVisible(b.getVisible());
        vo.setSort(b.getSortNum());
        return vo;
    }
}
