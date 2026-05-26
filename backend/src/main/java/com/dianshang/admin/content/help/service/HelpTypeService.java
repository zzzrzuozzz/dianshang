package com.dianshang.admin.content.help.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.content.dto.ContentPageVO;
import com.dianshang.admin.content.dto.LongIdsRequest;
import com.dianshang.admin.content.dto.TypeOptionVO;
import com.dianshang.admin.content.help.dto.HelpTypeSaveRequest;
import com.dianshang.admin.content.help.dto.HelpTypeVO;
import com.dianshang.admin.content.help.entity.CmsHelpTypeEntity;
import com.dianshang.admin.content.help.repository.CmsHelpArticleRepository;
import com.dianshang.admin.content.help.repository.CmsHelpTypeRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HelpTypeService {

    private final CmsHelpTypeRepository helpTypeRepository;
    private final CmsHelpArticleRepository articleRepository;

    public HelpTypeService(CmsHelpTypeRepository helpTypeRepository, CmsHelpArticleRepository articleRepository) {
        this.helpTypeRepository = helpTypeRepository;
        this.articleRepository = articleRepository;
    }

    public ContentPageVO<HelpTypeVO> list(String keyword, String tab, int page, int pageSize) {
        Specification<CmsHelpTypeEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            if (StringUtils.hasText(keyword)) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), kw),
                        cb.like(cb.lower(root.get("typeCode")), kw)
                ));
            }
            if ("active".equals(tab)) {
                predicates.add(cb.isTrue(root.get("visible")));
            } else if ("disabled".equals(tab)) {
                predicates.add(cb.isFalse(root.get("visible")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CmsHelpTypeEntity> result = helpTypeRepository.findAll(
                spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "sortNum", "id"))
        );

        ContentPageVO<HelpTypeVO> vo = new ContentPageVO<>();
        vo.setList(result.getContent().stream().map(this::toVO).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages(result.getTotalPages() == 0 ? 1 : (int) result.getTotalPages());
        vo.setCounts(typeCounts());
        return vo;
    }

    public List<TypeOptionVO> options() {
        Specification<CmsHelpTypeEntity> spec = (root, query, cb) -> cb.equal(root.get("deleted"), false);
        return helpTypeRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "sortNum"))
                .stream().map(t -> new TypeOptionVO(t.getId(), t.getName())).toList();
    }

    public HelpTypeVO detail(Long id) {
        return toVO(require(id));
    }

    @Transactional
    public Long save(HelpTypeSaveRequest request) {
        CmsHelpTypeEntity entity;
        if (request.getId() != null) {
            entity = require(request.getId());
        } else {
            entity = new CmsHelpTypeEntity();
            entity.setTypeCode(nextTypeCode());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setDeleted(false);
        }
        entity.setName(request.getName().trim());
        entity.setIcon(request.getIcon());
        entity.setVisible(request.getVisible() == null || request.getVisible());
        entity.setSortNum(request.getSort() == null ? 0 : request.getSort());
        helpTypeRepository.save(entity);
        return entity.getId();
    }

    @Transactional
    public void toggleVisible(Long id, boolean visible) {
        CmsHelpTypeEntity entity = require(id);
        entity.setVisible(visible);
        helpTypeRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        CmsHelpTypeEntity entity = require(id);
        entity.setDeleted(true);
        helpTypeRepository.save(entity);
    }

    @Transactional
    public int batchDelete(LongIdsRequest request) {
        int count = 0;
        for (Long id : request.getIds()) {
            if (id == null) {
                continue;
            }
            helpTypeRepository.findByIdAndDeletedFalse(id).ifPresent(e -> {
                e.setDeleted(true);
                helpTypeRepository.save(e);
            });
            count++;
        }
        return count;
    }

    private CmsHelpTypeEntity require(Long id) {
        return helpTypeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("帮助分类不存在"));
    }

    private HelpTypeVO toVO(CmsHelpTypeEntity e) {
        HelpTypeVO vo = new HelpTypeVO();
        vo.setId(e.getId());
        vo.setCode(e.getTypeCode());
        vo.setName(e.getName());
        vo.setIcon(e.getIcon());
        vo.setVisible(e.getVisible());
        vo.setSort(e.getSortNum());
        vo.setArticleCount((int) articleRepository.countByTypeIdAndDeletedFalse(e.getId()));
        return vo;
    }

    private Map<String, Integer> typeCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("all", (int) helpTypeRepository.countByDeletedFalse());
        counts.put("active", (int) helpTypeRepository.countByDeletedFalseAndVisibleTrue());
        counts.put("disabled", (int) helpTypeRepository.countByDeletedFalseAndVisibleFalse());
        return counts;
    }

    private String nextTypeCode() {
        return "HT" + (6542 + helpTypeRepository.count());
    }
}
