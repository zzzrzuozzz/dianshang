package com.dianshang.admin.content.topic.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.content.dto.ContentPageVO;
import com.dianshang.admin.content.dto.IdsRequest;
import com.dianshang.admin.content.topic.dto.*;
import com.dianshang.admin.content.topic.entity.CmsTopicCommentEntity;
import com.dianshang.admin.content.topic.entity.CmsTopicEntity;
import com.dianshang.admin.content.topic.entity.CmsTopicTypeEntity;
import com.dianshang.admin.content.topic.repository.CmsTopicCommentRepository;
import com.dianshang.admin.content.topic.repository.CmsTopicRepository;
import com.dianshang.admin.content.topic.repository.CmsTopicTypeRepository;
import com.dianshang.admin.ops.support.OpsAudienceSupport;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.repository.ProductRepository;
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
import java.util.*;

@Service
public class TopicService {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");
    private static final Map<Integer, String> COMMENT_STATUS = Map.of(
            0, "待审核",
            1, "已加精",
            2, "已隐藏"
    );

    private final CmsTopicRepository topicRepository;
    private final CmsTopicTypeRepository topicTypeRepository;
    private final CmsTopicCommentRepository commentRepository;
    private final ProductRepository productRepository;

    public TopicService(CmsTopicRepository topicRepository,
                        CmsTopicTypeRepository topicTypeRepository,
                        CmsTopicCommentRepository commentRepository,
                        ProductRepository productRepository) {
        this.topicRepository = topicRepository;
        this.topicTypeRepository = topicTypeRepository;
        this.commentRepository = commentRepository;
        this.productRepository = productRepository;
    }

    public ContentPageVO<TopicListVO> list(String title, String tab,
                                           LocalDate startDate, LocalDate endDate,
                                           int page, int pageSize) {
        Specification<CmsTopicEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            if (StringUtils.hasText(title)) {
                String kw = "%" + title.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), kw),
                        cb.like(cb.lower(root.get("topicCode")), kw)
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

        Page<CmsTopicEntity> result = topicRepository.findAll(
                spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "sortNum", "createdAt"))
        );

        Map<Long, String> typeNames = typeNameMap();
        ContentPageVO<TopicListVO> vo = new ContentPageVO<>();
        vo.setList(result.getContent().stream().map(t -> toListVO(t, typeNames)).toList());
        vo.setTotal(result.getTotalElements());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTotalPages(result.getTotalPages() == 0 ? 1 : (int) result.getTotalPages());
        return vo;
    }

    public TopicDetailVO detail(String topicCode) {
        CmsTopicEntity topic = requireTopic(topicCode);
        TopicDetailVO vo = toFormVO(topic);
        vo.setProducts(loadProducts(Jsons.toStringList(topic.getProductIdsJson())));
        vo.setComments(commentRepository.findByTopicCodeAndDeletedFalseOrderByCreatedAtDesc(topicCode)
                .stream().map(this::toCommentVO).toList());
        return vo;
    }

    public TopicDetailVO editForm(String topicCode) {
        return toFormVO(requireTopic(topicCode));
    }

    @Transactional
    public String save(TopicSaveRequest request) {
        requireType(request.getTypeId());
        CmsTopicEntity entity;
        if (StringUtils.hasText(request.getTopicCode())) {
            entity = requireTopic(request.getTopicCode());
        } else {
            entity = new CmsTopicEntity();
            entity.setTopicCode(nextTopicCode());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setDeleted(false);
            entity.setClickCount(0);
            entity.setCollectCount(0);
            entity.setCommentCount(0);
            entity.setReadCount(0);
            entity.setShareCount(0);
        }

        entity.setTypeId(request.getTypeId());
        entity.setTitle(request.getTitle().trim());
        entity.setIntro(request.getIntro());
        entity.setContentHtml(request.getContent());
        entity.setCoverImage(request.getCoverImage());
        List<String> images = request.getImages() != null ? request.getImages() : Collections.emptyList();
        if (images.isEmpty() && StringUtils.hasText(request.getCoverImage())) {
            images = List.of(request.getCoverImage());
        }
        entity.setImagesJson(Jsons.toJson(images));
        entity.setSpecifyProducts(Boolean.TRUE.equals(request.getSpecifyProducts()));
        entity.setProductIdsJson(Jsons.toJson(
                entity.getSpecifyProducts() && request.getProductIds() != null ? request.getProductIds() : List.of()
        ));
        entity.setAudienceJson(OpsAudienceSupport.buildAudienceJson(
                request.getMemberLevels(), request.getRegions(), request.getTags(), null));
        entity.setSortNum(request.getSort() == null ? 0 : request.getSort());

        int status = request.getStatus() == null ? 1 : request.getStatus();
        applyStatus(entity, status);

        topicRepository.save(entity);
        return entity.getTopicCode();
    }

    @Transactional
    public void updateStatus(String topicCode, int status) {
        CmsTopicEntity entity = requireTopic(topicCode);
        applyStatus(entity, status);
        topicRepository.save(entity);
    }

    @Transactional
    public void delete(String topicCode) {
        CmsTopicEntity entity = requireTopic(topicCode);
        entity.setDeleted(true);
        topicRepository.save(entity);
    }

    @Transactional
    public void replyComment(TopicCommentReplyRequest request) {
        CmsTopicCommentEntity comment = requireComment(request.getCommentCode());
        comment.setReplyContent(request.getReplyContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void reviewComment(TopicCommentReviewRequest request) {
        CmsTopicCommentEntity comment = requireComment(request.getCommentCode());
        switch (request.getAction()) {
            case "feature" -> comment.setStatus(1);
            case "hide" -> comment.setStatus(2);
            case "show", "unhide" -> comment.setStatus(0);
            default -> throw new BusinessException("不支持的操作");
        }
        commentRepository.save(comment);
        refreshCommentCount(comment.getTopicCode());
    }

    @Transactional
    public void deleteComment(String commentCode) {
        CmsTopicCommentEntity comment = requireComment(commentCode);
        String topicCode = comment.getTopicCode();
        comment.setDeleted(true);
        commentRepository.save(comment);
        refreshCommentCount(topicCode);
    }

    @Transactional
    public int batchDeleteComments(IdsRequest request) {
        int count = 0;
        String topicCode = null;
        for (String code : request.getIds()) {
            if (!StringUtils.hasText(code)) {
                continue;
            }
            var found = commentRepository.findByCommentCodeAndDeletedFalse(code.trim());
            if (found.isPresent()) {
                CmsTopicCommentEntity c = found.get();
                topicCode = c.getTopicCode();
                c.setDeleted(true);
                commentRepository.save(c);
                count++;
            }
        }
        if (topicCode != null) {
            refreshCommentCount(topicCode);
        }
        return count;
    }

    private void applyStatus(CmsTopicEntity entity, int status) {
        entity.setStatus(status);
        if (status == 1 && entity.getPublishedAt() == null) {
            entity.setPublishedAt(LocalDateTime.now());
        }
    }

    private void refreshCommentCount(String topicCode) {
        topicRepository.findByTopicCodeAndDeletedFalse(topicCode).ifPresent(t -> {
            t.setCommentCount((int) commentRepository.countByTopicCodeAndDeletedFalse(topicCode));
            topicRepository.save(t);
        });
    }

    private CmsTopicEntity requireTopic(String code) {
        return topicRepository.findByTopicCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("专题不存在"));
    }

    private CmsTopicCommentEntity requireComment(String code) {
        return commentRepository.findByCommentCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("评论不存在"));
    }

    private CmsTopicTypeEntity requireType(Long typeId) {
        return topicTypeRepository.findByIdAndDeletedFalse(typeId)
                .orElseThrow(() -> new BusinessException("专题类型不存在"));
    }

    private TopicListVO toListVO(CmsTopicEntity t, Map<Long, String> typeNames) {
        TopicListVO vo = new TopicListVO();
        vo.setId(t.getTopicCode());
        vo.setTypeId(t.getTypeId());
        vo.setTypeName(typeNames.getOrDefault(t.getTypeId(), "-"));
        vo.setTitle(t.getTitle());
        LocalDateTime pub = t.getPublishedAt() != null ? t.getPublishedAt() : t.getCreatedAt();
        vo.setPublishTime(pub == null ? "-" : pub.format(DISPLAY));
        List<String> pids = Jsons.toStringList(t.getProductIdsJson());
        vo.setProductCount(pids.size());
        vo.setClickCount(t.getClickCount());
        vo.setCollectCount(t.getCollectCount());
        vo.setCommentCount(t.getCommentCount());
        vo.setSort(t.getSortNum());
        vo.setStatus(t.getStatus());
        vo.setStatusText(t.getStatus() != null && t.getStatus() == 1 ? "已上线" : "已下架");
        return vo;
    }

    private TopicDetailVO toFormVO(CmsTopicEntity t) {
        TopicDetailVO vo = new TopicDetailVO();
        vo.setId(t.getTopicCode());
        vo.setTypeId(t.getTypeId());
        vo.setTitle(t.getTitle());
        vo.setIntro(t.getIntro());
        vo.setContent(t.getContentHtml());
        vo.setCoverImage(t.getCoverImage());
        List<String> images = Jsons.toStringList(t.getImagesJson());
        vo.setImages(images.isEmpty() && StringUtils.hasText(t.getCoverImage())
                ? List.of(t.getCoverImage()) : images);
        vo.setSpecifyProducts(Boolean.TRUE.equals(t.getSpecifyProducts()));
        vo.setProductIds(Jsons.toStringList(t.getProductIdsJson()));
        vo.setSort(t.getSortNum());
        vo.setStatus(t.getStatus());
        vo.setCollectCount(t.getCollectCount());
        vo.setReadCount(t.getReadCount());
        vo.setShareCount(t.getShareCount());

        Map<String, Object> audience = OpsAudienceSupport.parseAudience(t.getAudienceJson());
        List<String> levels = new ArrayList<>();
        List<List<String>> regions = new ArrayList<>();
        Map<String, List<String>> tags = new LinkedHashMap<>();
        List<String> pushMethod = new ArrayList<>();
        OpsAudienceSupport.fillAudienceFields(audience, levels, regions, tags, pushMethod);
        vo.setMemberLevels(levels.isEmpty() ? List.of("all") : levels);
        vo.setRegions(regions);
        vo.setTags(tags.isEmpty() ? defaultTags() : tags);
        return vo;
    }

    private Map<String, List<String>> defaultTags() {
        Map<String, List<String>> tags = new LinkedHashMap<>();
        tags.put("newUser", List.of("all"));
        tags.put("firstBuy", List.of("all"));
        tags.put("repurchase", List.of("all"));
        tags.put("active", List.of("all"));
        return tags;
    }

    private TopicCommentVO toCommentVO(CmsTopicCommentEntity c) {
        TopicCommentVO vo = new TopicCommentVO();
        vo.setId(c.getCommentCode());
        vo.setContent(c.getContent());
        vo.setReplyContent(c.getReplyContent());
        vo.setPics(Jsons.toStringList(c.getPicsJson()));
        vo.setStatus(c.getStatus());
        vo.setStatusText(COMMENT_STATUS.getOrDefault(c.getStatus(), "待审核"));
        return vo;
    }

    private List<TopicProductVO> loadProducts(List<String> productNos) {
        if (productNos == null || productNos.isEmpty()) {
            return List.of();
        }
        List<TopicProductVO> list = new ArrayList<>();
        for (String no : productNos) {
            productRepository.findByProductNoAndDeletedFalse(no).ifPresent(p -> list.add(toProductVO(p)));
        }
        return list;
    }

    private TopicProductVO toProductVO(Product p) {
        TopicProductVO vo = new TopicProductVO();
        vo.setId(p.getProductNo());
        vo.setName(p.getTitle());
        vo.setPrice(p.getDiscountPrice());
        vo.setThumb(p.getThumb());
        return vo;
    }

    private Map<Long, String> typeNameMap() {
        Map<Long, String> map = new HashMap<>();
        topicTypeRepository.findAll().stream()
                .filter(t -> !Boolean.TRUE.equals(t.getDeleted()))
                .forEach(t -> map.put(t.getId(), t.getName()));
        return map;
    }

    private String nextTopicCode() {
        long base = 6542 + topicRepository.count();
        for (int i = 0; i < 100; i++) {
            String code = "BZ" + (base + i);
            if (topicRepository.findByTopicCodeAndDeletedFalse(code).isEmpty()) {
                return code;
            }
        }
        return "BZ" + System.currentTimeMillis();
    }
}
