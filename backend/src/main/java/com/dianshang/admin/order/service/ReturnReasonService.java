package com.dianshang.admin.order.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.order.dto.ReturnReasonSaveRequest;
import com.dianshang.admin.order.dto.ReturnReasonVO;
import com.dianshang.admin.order.entity.ReturnReasonEntity;
import com.dianshang.admin.order.repository.ReturnReasonRepository;
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
public class ReturnReasonService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");

    private final ReturnReasonRepository returnReasonRepository;

    public ReturnReasonService(ReturnReasonRepository returnReasonRepository) {
        this.returnReasonRepository = returnReasonRepository;
    }

    public PageResult<ReturnReasonVO> list(String keyword, int page, int pageSize) {
        Specification<ReturnReasonEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("reasonText"), like),
                        cb.like(root.get("reasonCode"), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ReturnReasonEntity> result = returnReasonRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "sortNum")));
        List<ReturnReasonVO> list = result.getContent().stream().map(this::toVO).toList();
        return new PageResult<>(list, result.getTotalElements(), page, pageSize);
    }

    @Transactional
    public ReturnReasonVO create(ReturnReasonSaveRequest request) {
        ReturnReasonEntity entity = new ReturnReasonEntity();
        entity.setReasonCode("RR" + System.currentTimeMillis() % 100000);
        entity.setReasonText(request.getReason().trim());
        entity.setVisible(request.getVisible() != null ? request.getVisible() : true);
        entity.setSortNum(request.getSort() != null ? request.getSort() : 0);
        return toVO(returnReasonRepository.save(entity));
    }

    @Transactional
    public ReturnReasonVO update(String code, ReturnReasonSaveRequest request) {
        ReturnReasonEntity entity = require(code);
        entity.setReasonText(request.getReason().trim());
        if (request.getSort() != null) {
            entity.setSortNum(request.getSort());
        }
        return toVO(returnReasonRepository.save(entity));
    }

    @Transactional
    public void delete(String code) {
        ReturnReasonEntity entity = require(code);
        entity.setDeleted(true);
        returnReasonRepository.save(entity);
    }

    @Transactional
    public void updateVisible(String code, boolean visible) {
        ReturnReasonEntity entity = require(code);
        entity.setVisible(visible);
        returnReasonRepository.save(entity);
    }

    private ReturnReasonEntity require(String code) {
        return returnReasonRepository.findByReasonCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("退货原因不存在"));
    }

    private ReturnReasonVO toVO(ReturnReasonEntity e) {
        ReturnReasonVO vo = new ReturnReasonVO();
        vo.setId(e.getReasonCode());
        vo.setReason(e.getReasonText());
        vo.setAddTime(e.getAddTime().format(FMT));
        vo.setVisible(e.getVisible());
        vo.setSort(e.getSortNum());
        return vo;
    }
}
