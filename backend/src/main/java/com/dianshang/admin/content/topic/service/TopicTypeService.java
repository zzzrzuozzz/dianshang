package com.dianshang.admin.content.topic.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.content.dto.ContentPageVO;
import com.dianshang.admin.content.dto.LongIdsRequest;
import com.dianshang.admin.content.dto.TypeOptionVO;
import com.dianshang.admin.content.topic.dto.TopicTypeSaveRequest;
import com.dianshang.admin.content.topic.dto.TopicTypeVO;
import com.dianshang.admin.content.topic.entity.CmsTopicTypeEntity;
import com.dianshang.admin.content.topic.repository.CmsTopicRepository;
import com.dianshang.admin.content.topic.repository.CmsTopicTypeRepository;
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
public class TopicTypeService {

    private final CmsTopicTypeRepository topicTypeRepository;
    private final CmsTopicRepository topicRepository;

    public TopicTypeService(CmsTopicTypeRepository topicTypeRepository, CmsTopicRepository topicRepository) {
        this.topicTypeRepository = topicTypeRepository;
        this.topicRepository = topicRepository;
    }

    public ContentPageVO<TopicTypeVO> list(String keyword, String tab, int page, int pageSize) {
        Specification<CmsTopicTypeEntity> spec = (root, query, cb) -> {
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

        Page<CmsTopicTypeEntity> result = topicTypeRepository.findAll(
                spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "sortNum", "id"))
        );

        ContentPageVO<TopicTypeVO> vo = new ContentPageVO<>();
        vo.setList(result.getContent().stream().map(this::toVO).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages(result.getTotalPages() == 0 ? 1 : (int) result.getTotalPages());
        vo.setCounts(typeCounts());
        return vo;
    }

    public List<TypeOptionVO> options() {
        return topicTypeRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("deleted"), false),
                Sort.by(Sort.Direction.DESC, "sortNum")
        ).stream().map(t -> new TypeOptionVO(t.getId(), t.getName())).toList();
    }

    public TopicTypeVO detail(Long id) {
        return toVO(require(id));
    }

    @Transactional
    public Long save(TopicTypeSaveRequest request) {
        CmsTopicTypeEntity entity;
        if (request.getId() != null) {
            entity = require(request.getId());
        } else {
            entity = new CmsTopicTypeEntity();
            entity.setTypeCode(nextTypeCode());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setDeleted(false);
        }
        entity.setName(request.getName().trim());
        entity.setIntro(request.getIntro());
        entity.setIcon(request.getIcon());
        entity.setVisible(request.getVisible() == null || request.getVisible());
        entity.setSortNum(request.getSort() == null ? 0 : request.getSort());
        topicTypeRepository.save(entity);
        return entity.getId();
    }

    @Transactional
    public void toggleVisible(Long id, boolean visible) {
        CmsTopicTypeEntity entity = require(id);
        entity.setVisible(visible);
        topicTypeRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        CmsTopicTypeEntity entity = require(id);
        entity.setDeleted(true);
        topicTypeRepository.save(entity);
    }

    @Transactional
    public int batchDelete(LongIdsRequest request) {
        int count = 0;
        for (Long id : request.getIds()) {
            if (id == null) {
                continue;
            }
            topicTypeRepository.findByIdAndDeletedFalse(id).ifPresent(e -> {
                e.setDeleted(true);
                topicTypeRepository.save(e);
            });
            count++;
        }
        return count;
    }

    private CmsTopicTypeEntity require(Long id) {
        return topicTypeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("专题类型不存在"));
    }

    private TopicTypeVO toVO(CmsTopicTypeEntity e) {
        TopicTypeVO vo = new TopicTypeVO();
        vo.setId(e.getId());
        vo.setCode(e.getTypeCode());
        vo.setName(e.getName());
        vo.setIcon(e.getIcon());
        vo.setIntro(e.getIntro());
        vo.setVisible(e.getVisible());
        vo.setSort(e.getSortNum());
        vo.setTopicCount((int) topicRepository.countByTypeIdAndDeletedFalse(e.getId()));
        return vo;
    }

    private Map<String, Integer> typeCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("all", (int) topicTypeRepository.countByDeletedFalse());
        counts.put("active", (int) topicTypeRepository.countByDeletedFalseAndVisibleTrue());
        counts.put("disabled", (int) topicTypeRepository.countByDeletedFalseAndVisibleFalse());
        return counts;
    }

    private String nextTypeCode() {
        long base = 6542 + topicTypeRepository.count();
        for (int i = 0; i < 100; i++) {
            String code = "TT" + (base + i);
            if (topicTypeRepository.findAll().stream().noneMatch(t -> code.equals(t.getTypeCode()))) {
                return code;
            }
        }
        return "TT" + System.currentTimeMillis();
    }
}
