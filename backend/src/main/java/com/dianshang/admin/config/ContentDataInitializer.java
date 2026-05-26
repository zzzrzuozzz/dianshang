package com.dianshang.admin.config;

import com.dianshang.admin.common.Jsons;
import com.dianshang.admin.content.help.entity.CmsHelpArticleEntity;
import com.dianshang.admin.content.help.entity.CmsHelpTypeEntity;
import com.dianshang.admin.content.help.repository.CmsHelpArticleRepository;
import com.dianshang.admin.content.help.repository.CmsHelpTypeRepository;
import com.dianshang.admin.content.topic.entity.CmsTopicCommentEntity;
import com.dianshang.admin.content.topic.entity.CmsTopicEntity;
import com.dianshang.admin.content.topic.entity.CmsTopicTypeEntity;
import com.dianshang.admin.content.topic.repository.CmsTopicCommentRepository;
import com.dianshang.admin.content.topic.repository.CmsTopicRepository;
import com.dianshang.admin.content.topic.repository.CmsTopicTypeRepository;
import com.dianshang.admin.ops.support.OpsAudienceSupport;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ContentDataInitializer implements CommandLineRunner {

    private final CmsTopicTypeRepository topicTypeRepository;
    private final CmsTopicRepository topicRepository;
    private final CmsTopicCommentRepository commentRepository;
    private final CmsHelpTypeRepository helpTypeRepository;
    private final CmsHelpArticleRepository helpArticleRepository;

    public ContentDataInitializer(CmsTopicTypeRepository topicTypeRepository,
                                  CmsTopicRepository topicRepository,
                                  CmsTopicCommentRepository commentRepository,
                                  CmsHelpTypeRepository helpTypeRepository,
                                  CmsHelpArticleRepository helpArticleRepository) {
        this.topicTypeRepository = topicTypeRepository;
        this.topicRepository = topicRepository;
        this.commentRepository = commentRepository;
        this.helpTypeRepository = helpTypeRepository;
        this.helpArticleRepository = helpArticleRepository;
    }

    @Override
    public void run(String... args) {
        if (topicTypeRepository.count() == 0) {
            seedTopicTypes();
        }
        if (topicRepository.count() == 0) {
            seedTopics();
        }
        if (commentRepository.count() == 0) {
            seedComments();
        }
        if (helpTypeRepository.count() == 0) {
            seedHelpTypes();
        }
        if (helpArticleRepository.count() == 0) {
            seedHelpArticles();
        }
    }

    private void seedTopicTypes() {
        saveTopicType("TT6542", "服装专题", true, 10, "服装类专题");
        saveTopicType("TT6543", "家居服饰", true, 9, "家居类");
        saveTopicType("TT6544", "餐饮厨房", false, 8, "厨房类");
    }

    private CmsTopicTypeEntity saveTopicType(String code, String name, boolean visible, int sort, String intro) {
        CmsTopicTypeEntity e = new CmsTopicTypeEntity();
        e.setTypeCode(code);
        e.setName(name);
        e.setVisible(visible);
        e.setSortNum(sort);
        e.setIntro(intro);
        e.setIcon(null);
        e.setDeleted(false);
        e.setCreatedAt(LocalDateTime.now());
        return topicTypeRepository.save(e);
    }

    private void seedTopics() {
        Long homeId = topicTypeRepository.findAll().stream()
                .filter(t -> "家居服饰".equals(t.getName()))
                .map(CmsTopicTypeEntity::getId)
                .findFirst().orElse(1L);
        Long kitchenId = topicTypeRepository.findAll().stream()
                .filter(t -> "餐饮厨房".equals(t.getName()))
                .map(CmsTopicTypeEntity::getId)
                .findFirst().orElse(1L);

        saveTopic("BZ6542", homeId, "春季家电家具疯狂秒杀", 1, 10, 1000, 1000, 3,
                List.of("025342", "025343", "025344"));
        saveTopic("BZ6543", kitchenId, "夏日清凉厨房好物", 0, 5, 500, 200, 0, List.of());
    }

    private void saveTopic(String code, Long typeId, String title, int status, int productCount,
                           int clicks, int collects, int comments, List<String> productIds) {
        CmsTopicEntity e = new CmsTopicEntity();
        e.setTopicCode(code);
        e.setTypeId(typeId);
        e.setTitle(title);
        e.setIntro("专题简介：" + title);
        e.setContentHtml("春季大促专题详情，精选好物限时优惠。");
        e.setCoverImage(null);
        e.setImagesJson(Jsons.toJson(List.of()));
        e.setSpecifyProducts(!productIds.isEmpty());
        e.setProductIdsJson(Jsons.toJson(productIds));
        e.setAudienceJson(OpsAudienceSupport.buildAudienceJson(List.of("all"), List.of(), defaultTags(), null));
        e.setSortNum(2);
        e.setStatus(status);
        e.setClickCount(clicks);
        e.setCollectCount(collects);
        e.setCommentCount(comments);
        e.setReadCount(1);
        e.setShareCount(100);
        e.setPublishedAt(LocalDateTime.of(2024, 8, 8, 16, 14));
        e.setDeleted(false);
        e.setCreatedAt(e.getPublishedAt());
        topicRepository.save(e);
    }

    private void seedComments() {
        saveComment("TC6542", "BZ6542", "挺好的，不错", "感谢支持，我们会继续加油的", 1, List.of());
        saveComment("TC6543", "BZ6542", "垃圾", "", 2, List.of());
        saveComment("TC6544", "BZ6542", "期待更多活动", "", 0, List.of());
    }

    private void saveComment(String code, String topicCode, String content, String reply, int status, List<String> pics) {
        CmsTopicCommentEntity c = new CmsTopicCommentEntity();
        c.setCommentCode(code);
        c.setTopicCode(topicCode);
        c.setContent(content);
        c.setReplyContent(reply);
        c.setPicsJson(Jsons.toJson(pics));
        c.setStatus(status);
        c.setDeleted(false);
        c.setCreatedAt(LocalDateTime.now());
        commentRepository.save(c);
    }

    private void seedHelpTypes() {
        saveHelpType("HT6542", "新手教程", true, 10);
        saveHelpType("HT6543", "常见问题", true, 9);
        saveHelpType("HT6544", "售后政策", false, 8);
    }

    private void saveHelpType(String code, String name, boolean visible, int sort) {
        CmsHelpTypeEntity e = new CmsHelpTypeEntity();
        e.setTypeCode(code);
        e.setName(name);
        e.setVisible(visible);
        e.setSortNum(sort);
        e.setIcon(null);
        e.setDeleted(false);
        e.setCreatedAt(LocalDateTime.now());
        helpTypeRepository.save(e);
    }

    private void seedHelpArticles() {
        Long faqId = helpTypeRepository.findAll().stream()
                .filter(t -> "常见问题".equals(t.getName()))
                .map(CmsHelpTypeEntity::getId)
                .findFirst().orElse(1L);
        Long newbieId = helpTypeRepository.findAll().stream()
                .filter(t -> "新手教程".equals(t.getName()))
                .map(CmsHelpTypeEntity::getId)
                .findFirst().orElse(1L);

        saveArticle("HA6542", faqId, "常见问题", 1, 1000);
        saveArticle("HA6543", newbieId, "如何注册会员", 0, 800);
    }

    private void saveArticle(String code, Long typeId, String title, int status, int clicks) {
        CmsHelpArticleEntity e = new CmsHelpArticleEntity();
        e.setArticleCode(code);
        e.setTypeId(typeId);
        e.setTitle(title);
        e.setIntro("帮助简介");
        e.setContentHtml("帮助详情正文，解答用户常见疑问。");
        e.setCoverImagesJson(Jsons.toJson(List.of()));
        e.setSortNum(2);
        e.setStatus(status);
        e.setClickCount(clicks);
        e.setPublishedAt(LocalDateTime.of(2024, 8, 8, 16, 14));
        e.setDeleted(false);
        e.setCreatedAt(e.getPublishedAt());
        helpArticleRepository.save(e);
    }

    private Map<String, List<String>> defaultTags() {
        return Map.of(
                "newUser", List.of("all"),
                "firstBuy", List.of("all"),
                "repurchase", List.of("all"),
                "active", List.of("all")
        );
    }
}
