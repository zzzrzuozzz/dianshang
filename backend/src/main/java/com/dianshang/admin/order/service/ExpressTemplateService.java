package com.dianshang.admin.order.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.order.dto.ExpressTemplateSaveRequest;
import com.dianshang.admin.order.dto.ExpressTemplateVO;
import com.dianshang.admin.order.entity.ExpressTemplateEntity;
import com.dianshang.admin.order.repository.ExpressTemplateRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpressTemplateService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");

    private final ExpressTemplateRepository expressTemplateRepository;

    public ExpressTemplateService(ExpressTemplateRepository expressTemplateRepository) {
        this.expressTemplateRepository = expressTemplateRepository;
    }

    public PageResult<ExpressTemplateVO> list(String keyword, int page, int pageSize) {
        Specification<ExpressTemplateEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("templateName"), like),
                        cb.like(root.get("expressCompany"), like),
                        cb.like(root.get("templateCode"), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ExpressTemplateEntity> result = expressTemplateRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize,
                        Sort.by(Sort.Direction.DESC, "sortNum", "id")));
        return new PageResult<>(result.getContent().stream().map(this::toVO).toList(),
                result.getTotalElements(), page, pageSize);
    }

    @Transactional
    public ExpressTemplateVO create(ExpressTemplateSaveRequest request) {
        ExpressTemplateEntity entity = new ExpressTemplateEntity();
        entity.setTemplateCode("ET" + System.currentTimeMillis() % 100000);
        applySave(entity, request);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultExcept(null);
            entity.setIsDefault(true);
        }
        return toVO(expressTemplateRepository.save(entity));
    }

    @Transactional
    public ExpressTemplateVO update(String code, ExpressTemplateSaveRequest request) {
        ExpressTemplateEntity entity = require(code);
        applySave(entity, request);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultExcept(entity.getId());
            entity.setIsDefault(true);
        }
        return toVO(expressTemplateRepository.save(entity));
    }

    @Transactional
    public void delete(String code) {
        ExpressTemplateEntity entity = require(code);
        entity.setDeleted(true);
        expressTemplateRepository.save(entity);
    }

    @Transactional
    public void updateVisible(String code, boolean visible) {
        ExpressTemplateEntity entity = require(code);
        entity.setVisible(visible);
        expressTemplateRepository.save(entity);
    }

    @Transactional
    public void updateDefault(String code, boolean isDefault) {
        ExpressTemplateEntity entity = require(code);
        if (isDefault) {
            clearDefaultExcept(entity.getId());
            entity.setIsDefault(true);
            entity.setVisible(true);
        } else {
            entity.setIsDefault(false);
        }
        expressTemplateRepository.save(entity);
    }

    public ExpressTemplateEntity findDefault() {
        return expressTemplateRepository.findByDeletedFalseAndIsDefaultTrue().stream()
                .filter(e -> Boolean.TRUE.equals(e.getVisible()))
                .findFirst()
                .orElse(null);
    }

    private void applySave(ExpressTemplateEntity entity, ExpressTemplateSaveRequest request) {
        entity.setTemplateName(request.getTemplateName().trim());
        entity.setExpressCompany(request.getExpressCompany().trim());
        entity.setTemplateSpec(trimOrEmpty(request.getTemplateSpec()));
        entity.setRemark(trimOrEmpty(request.getRemark()));
        if (request.getVisible() != null) {
            entity.setVisible(request.getVisible());
        }
        if (request.getSort() != null) {
            entity.setSortNum(request.getSort());
        }
    }

    private void clearDefaultExcept(Long keepId) {
        expressTemplateRepository.findAll((root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.isTrue(root.get("isDefault"))
        )).forEach(e -> {
            if (keepId == null || !keepId.equals(e.getId())) {
                e.setIsDefault(false);
                expressTemplateRepository.save(e);
            }
        });
    }

    private ExpressTemplateEntity require(String code) {
        return expressTemplateRepository.findByTemplateCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("快递单模板不存在"));
    }

    private ExpressTemplateVO toVO(ExpressTemplateEntity e) {
        ExpressTemplateVO vo = new ExpressTemplateVO();
        vo.setId(e.getTemplateCode());
        vo.setTemplateName(e.getTemplateName());
        vo.setExpressCompany(e.getExpressCompany());
        vo.setTemplateSpec(e.getTemplateSpec());
        vo.setRemark(e.getRemark());
        vo.setIsDefault(e.getIsDefault());
        vo.setVisible(e.getVisible());
        vo.setSort(e.getSortNum());
        vo.setAddTime(e.getAddTime().format(FMT));
        return vo;
    }

    private static String trimOrEmpty(String v) {
        return v != null ? v.trim() : "";
    }
}
