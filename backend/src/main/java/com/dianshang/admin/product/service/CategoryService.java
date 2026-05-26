package com.dianshang.admin.product.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.common.TreeNodeVO;
import com.dianshang.admin.product.dto.CategoryOptionVO;
import com.dianshang.admin.product.dto.CategoryVO;
import com.dianshang.admin.product.entity.ProductCategory;
import com.dianshang.admin.product.repository.ProductCategoryRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final String[] LEVEL_LABELS = {"", "一级", "二级", "三级", "四级"};

    private final ProductCategoryRepository categoryRepository;

    public CategoryService(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<TreeNodeVO> tree() {
        List<ProductCategory> all = categoryRepository.findByDeletedFalseOrderBySortNumAsc();
        Map<Long, List<ProductCategory>> byParent = all.stream()
                .collect(Collectors.groupingBy(ProductCategory::getParentId));
        return buildTree(0L, byParent);
    }

    private List<TreeNodeVO> buildTree(Long parentId, Map<Long, List<ProductCategory>> byParent) {
        List<ProductCategory> children = byParent.getOrDefault(parentId, List.of());
        return children.stream().map(c -> {
            TreeNodeVO node = new TreeNodeVO();
            node.setId(c.getId());
            node.setLabel(c.getName());
            List<TreeNodeVO> sub = buildTree(c.getId(), byParent);
            if (!sub.isEmpty()) {
                node.setChildren(sub);
            }
            return node;
        }).toList();
    }

    public List<CategoryOptionVO> options() {
        return categoryRepository.findByDeletedFalseOrderBySortNumAsc().stream()
                .filter(c -> c.getParentId() == 0L)
                .map(c -> new CategoryOptionVO(c.getName(), c.getCode()))
                .toList();
    }

    public PageResult<CategoryVO> list(String keyword, Integer level, int page, int pageSize) {
        Specification<ProductCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("code"), like)
                ));
            }
            if (level != null && level > 0) {
                predicates.add(cb.equal(root.get("levelNum"), level));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ProductCategory> result = categoryRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by("sortNum").ascending()));
        List<CategoryVO> list = result.getContent().stream().map(this::toVO).toList();
        return new PageResult<>(list, result.getTotalElements(), page, pageSize);
    }

    @Transactional
    public CategoryVO save(CategoryVO request) {
        ProductCategory entity = new ProductCategory();
        entity.setCode("CAT" + System.currentTimeMillis());
        entity.setName(request.getName());
        entity.setLevelNum(parseLevel(request.getLevel()));
        entity.setProductCount(request.getCount() != null ? request.getCount() : 0);
        entity.setUnit(request.getUnit() != null ? request.getUnit() : "件");
        entity.setVisible(request.getVisible() != null ? request.getVisible() : true);
        entity.setSortNum(request.getSort() != null ? request.getSort() : 0);
        return toVO(categoryRepository.save(entity));
    }

    @Transactional
    public CategoryVO update(String code, CategoryVO request) {
        ProductCategory entity = requireByCode(code);
        if (StringUtils.hasText(request.getName())) {
            entity.setName(request.getName());
        }
        if (request.getCount() != null) {
            entity.setProductCount(request.getCount());
        }
        if (request.getUnit() != null) {
            entity.setUnit(request.getUnit());
        }
        if (request.getSort() != null) {
            entity.setSortNum(request.getSort());
        }
        return toVO(categoryRepository.save(entity));
    }

    @Transactional
    public void delete(String code) {
        ProductCategory entity = requireByCode(code);
        entity.setDeleted(true);
        categoryRepository.save(entity);
    }

    @Transactional
    public void updateVisible(String code, boolean visible) {
        ProductCategory entity = requireByCode(code);
        entity.setVisible(visible);
        categoryRepository.save(entity);
    }

    private ProductCategory requireByCode(String code) {
        return categoryRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("分类不存在"));
    }

    private CategoryVO toVO(ProductCategory c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getCode());
        vo.setName(c.getName());
        vo.setLevel(levelLabel(c.getLevelNum()));
        vo.setCount(c.getProductCount());
        vo.setUnit(c.getUnit());
        vo.setVisible(c.getVisible());
        vo.setSort(c.getSortNum());
        return vo;
    }

    private String levelLabel(int level) {
        if (level >= 1 && level < LEVEL_LABELS.length) {
            return LEVEL_LABELS[level];
        }
        return level + "级";
    }

    private int parseLevel(String level) {
        if (!StringUtils.hasText(level)) {
            return 1;
        }
        for (int i = 1; i < LEVEL_LABELS.length; i++) {
            if (LEVEL_LABELS[i].equals(level)) {
                return i;
            }
        }
        return 1;
    }
}
