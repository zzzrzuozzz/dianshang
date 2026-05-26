package com.dianshang.admin.content.help.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.content.dto.ContentPageVO;
import com.dianshang.admin.content.help.dto.HelpArticleListVO;
import com.dianshang.admin.content.help.dto.HelpArticleSaveRequest;
import com.dianshang.admin.content.help.entity.CmsHelpArticleEntity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HelpArticleService {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

    private final CmsHelpArticleRepository articleRepository;
    private final CmsHelpTypeRepository helpTypeRepository;

    public HelpArticleService(CmsHelpArticleRepository articleRepository, CmsHelpTypeRepository helpTypeRepository) {
        this.articleRepository = articleRepository;
        this.helpTypeRepository = helpTypeRepository;
    }

    public ContentPageVO<HelpArticleListVO> list(String title, String tab,
                                                 LocalDate startDate, LocalDate endDate,
                                                 int page, int pageSize) {
        Specification<CmsHelpArticleEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            if (StringUtils.hasText(title)) {
                String kw = "%" + title.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), kw),
                        cb.like(cb.lower(root.get("articleCode")), kw)
                ));
            }
            if ("online".equals(tab)) {
                predicates.add(cb.equal(root.get("status"), 1));
            } else if ("offline".equals(tab)) {
                predicates.add(cb.equal(root.get("status"), 0));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("publishedAt"), endDate.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CmsHelpArticleEntity> result = articleRepository.findAll(
                spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "sortNum", "createdAt"))
        );

        Map<Long, String> typeNames = typeNameMap();
        ContentPageVO<HelpArticleListVO> vo = new ContentPageVO<>();
        vo.setList(result.getContent().stream().map(a -> toListVO(a, typeNames)).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages(result.getTotalPages() == 0 ? 1 : (int) result.getTotalPages());
        return vo;
    }

    public HelpArticleSaveRequest detail(String articleCode) {
        CmsHelpArticleEntity entity = require(articleCode);
        HelpArticleSaveRequest vo = new HelpArticleSaveRequest();
        vo.setArticleCode(entity.getArticleCode());
        vo.setTypeId(entity.getTypeId());
        vo.setTitle(entity.getTitle());
        vo.setIntro(entity.getIntro());
        vo.setContent(entity.getContentHtml());
        vo.setCoverImages(Jsons.toStringList(entity.getCoverImagesJson()));
        vo.setOnline(entity.getStatus() != null && entity.getStatus() == 1);
        vo.setSort(entity.getSortNum());
        return vo;
    }

    @Transactional
    public String save(HelpArticleSaveRequest request) {
        requireType(request.getTypeId());
        CmsHelpArticleEntity entity;
        if (StringUtils.hasText(request.getArticleCode())) {
            entity = require(request.getArticleCode());
        } else {
            entity = new CmsHelpArticleEntity();
            entity.setArticleCode(nextArticleCode());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setDeleted(false);
            entity.setClickCount(0);
        }

        entity.setTypeId(request.getTypeId());
        entity.setTitle(request.getTitle().trim());
        entity.setIntro(request.getIntro());
        entity.setContentHtml(request.getContent());
        entity.setCoverImagesJson(Jsons.toJson(
                request.getCoverImages() != null ? request.getCoverImages() : List.of()
        ));
        entity.setSortNum(request.getSort() == null ? 0 : request.getSort());

        boolean online = request.getOnline() == null || request.getOnline();
        entity.setStatus(online ? 1 : 0);
        if (online && entity.getPublishedAt() == null) {
            entity.setPublishedAt(LocalDateTime.now());
        }

        articleRepository.save(entity);
        return entity.getArticleCode();
    }

    @Transactional
    public void updateStatus(String articleCode, int status) {
        CmsHelpArticleEntity entity = require(articleCode);
        entity.setStatus(status);
        if (status == 1 && entity.getPublishedAt() == null) {
            entity.setPublishedAt(LocalDateTime.now());
        }
        articleRepository.save(entity);
    }

    @Transactional
    public void delete(String articleCode) {
        CmsHelpArticleEntity entity = require(articleCode);
        entity.setDeleted(true);
        articleRepository.save(entity);
    }

    private CmsHelpArticleEntity require(String code) {
        return articleRepository.findByArticleCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("帮助文章不存在"));
    }

    private CmsHelpTypeEntity requireType(Long typeId) {
        return helpTypeRepository.findByIdAndDeletedFalse(typeId)
                .orElseThrow(() -> new BusinessException("帮助分类不存在"));
    }

    private HelpArticleListVO toListVO(CmsHelpArticleEntity a, Map<Long, String> typeNames) {
        HelpArticleListVO vo = new HelpArticleListVO();
        vo.setId(a.getArticleCode());
        vo.setTypeId(a.getTypeId());
        vo.setCategoryName(typeNames.getOrDefault(a.getTypeId(), "-"));
        vo.setTitle(a.getTitle());
        LocalDateTime pub = a.getPublishedAt() != null ? a.getPublishedAt() : a.getCreatedAt();
        vo.setPublishTime(pub == null ? "-" : pub.format(DISPLAY));
        vo.setClickCount(a.getClickCount());
        vo.setSort(a.getSortNum());
        vo.setStatus(a.getStatus());
        vo.setStatusText(a.getStatus() != null && a.getStatus() == 1 ? "已上线" : "已下架");
        return vo;
    }

    private Map<Long, String> typeNameMap() {
        Map<Long, String> map = new HashMap<>();
        helpTypeRepository.findAll().stream()
                .filter(t -> !Boolean.TRUE.equals(t.getDeleted()))
                .forEach(t -> map.put(t.getId(), t.getName()));
        return map;
    }

    private String nextArticleCode() {
        return "HA" + (6542 + articleRepository.count());
    }
}
