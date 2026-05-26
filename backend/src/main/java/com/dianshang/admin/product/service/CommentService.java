package com.dianshang.admin.product.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.TabCountVO;
import com.dianshang.admin.product.dto.CommentListVO;
import com.dianshang.admin.product.dto.CommentOverviewVO;
import com.dianshang.admin.product.dto.CommentPageVO;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.entity.ProductComment;
import com.dianshang.admin.product.repository.ProductCommentRepository;
import com.dianshang.admin.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
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
public class CommentService {

    private final ProductRepository productRepository;
    private final ProductCommentRepository commentRepository;

    public CommentService(ProductRepository productRepository, ProductCommentRepository commentRepository) {
        this.productRepository = productRepository;
        this.commentRepository = commentRepository;
    }

    public CommentOverviewVO overview() {
        CommentOverviewVO vo = new CommentOverviewVO();
        long bad = commentRepository.countByDeletedFalseAndRating("bad");
        long neutral = commentRepository.countByDeletedFalseAndRating("neutral");
        long feature = commentRepository.countByDeletedFalseAndFeaturedFalse();
        long total = commentRepository.countByDeletedFalse();
        long good = commentRepository.countByDeletedFalseAndRating("good");
        double goodRate = total > 0 ? good * 100.0 / total : 0;
        double badRate = total > 0 ? bad * 100.0 / total : 0;
        double neutralRate = total > 0 ? neutral * 100.0 / total : 0;

        vo.setStats(List.of(
                stat("badReply", "待回复差评", String.valueOf(bad), "去回复 >", 0, null),
                stat("neutralReply", "待回复中评", String.valueOf(neutral), "去回复 >", 2, null),
                stat("feature", "可加精评论", String.valueOf(feature), "去加精 >", 3, null),
                stat("goodRate", "近30天评价好评率", String.format("%.2f%%", goodRate), null, null, -5),
                stat("badRate", "近30天差评率", String.format("%.2f%%", badRate), null, null, 20),
                stat("neutralRate", "近30天评价中评率", String.format("%.2f%%", neutralRate), null, null, 10)
        ));
        return vo;
    }

    private CommentOverviewVO.CommentStatCardVO stat(String key, String label, String value,
                                                     String action, Integer todayNew, Integer trend) {
        CommentOverviewVO.CommentStatCardVO card = new CommentOverviewVO.CommentStatCardVO();
        card.setKey(key);
        card.setLabel(label);
        card.setValue(value);
        card.setAction(action);
        card.setTodayNew(todayNew);
        card.setTrend(trend);
        return card;
    }

    public CommentPageVO list(String keyword, String rating, int page, int pageSize) {
        List<ProductComment> comments = commentRepository.findAll(
                (root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isFalse(root.get("deleted")));
                    if (StringUtils.hasText(rating) && !"all".equals(rating)) {
                        predicates.add(cb.equal(root.get("rating"), rating));
                    }
                    return cb.and(predicates.toArray(new Predicate[0]));
                },
                Sort.by(Sort.Direction.DESC, "id")
        );

        Map<String, List<ProductComment>> byProduct = comments.stream()
                .collect(Collectors.groupingBy(ProductComment::getProductNo));

        List<String> productNos = new ArrayList<>(byProduct.keySet());
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim().toLowerCase();
            productNos = productNos.stream().filter(no -> {
                Product p = productRepository.findByProductNoAndDeletedFalse(no).orElse(null);
                return p != null && (p.getTitle().toLowerCase().contains(kw) || no.contains(kw));
            }).toList();
        }

        int from = Math.max((page - 1) * pageSize, 0);
        int to = Math.min(from + pageSize, productNos.size());
        List<String> pageNos = from < productNos.size() ? productNos.subList(from, to) : List.of();

        List<CommentListVO> list = pageNos.stream()
                .map(no -> buildRow(no, byProduct.get(no)))
                .filter(v -> v != null)
                .toList();

        CommentPageVO vo = new CommentPageVO();
        vo.setList(list);
        vo.setTotal(productNos.size());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setTabs(buildRatingTabs());
        return vo;
    }

    private List<TabCountVO> buildRatingTabs() {
        long all = commentRepository.countByDeletedFalse();
        return List.of(
                new TabCountVO("all", "全部", all),
                new TabCountVO("good", "好评", commentRepository.countByDeletedFalseAndRating("good")),
                new TabCountVO("neutral", "中评", commentRepository.countByDeletedFalseAndRating("neutral")),
                new TabCountVO("bad", "差评", commentRepository.countByDeletedFalseAndRating("bad"))
        );
    }

    private CommentListVO buildRow(String productNo, List<ProductComment> comments) {
        Product product = productRepository.findByProductNoAndDeletedFalse(productNo).orElse(null);
        if (product == null || comments == null || comments.isEmpty()) {
            return null;
        }
        ProductComment latest = comments.get(comments.size() - 1);
        CommentListVO vo = new CommentListVO();
        vo.setId(product.getProductNo());
        vo.setTitle(product.getTitle());
        vo.setThumb(product.getThumb());
        vo.setRating(latest.getRating());
        vo.setContent(latest.getContent());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setDiscountPrice(product.getDiscountPrice());
        vo.setStatus(product.getStatus());
        vo.setSku(product.getSku());
        vo.setSort(product.getSortNum());
        vo.setStock(product.getStock());
        vo.setSales(product.getTotalSales());
        vo.setTotalGood((int) comments.stream().filter(c -> "good".equals(c.getRating())).count());
        vo.setTotalNeutral((int) comments.stream().filter(c -> "neutral".equals(c.getRating())).count());
        vo.setTotalBad((int) comments.stream().filter(c -> "bad".equals(c.getRating())).count());
        return vo;
    }

    @Transactional
    public void featureByProductNo(String productNo) {
        List<ProductComment> comments = commentRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.isFalse(root.get("deleted")),
                        cb.equal(root.get("productNo"), productNo)
                ));
        if (comments.isEmpty()) {
            throw new BusinessException("评价不存在");
        }
        comments.forEach(c -> c.setFeatured(true));
        commentRepository.saveAll(comments);
    }

    @Transactional
    public void deleteByProductNo(String productNo) {
        List<ProductComment> comments = commentRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.isFalse(root.get("deleted")),
                        cb.equal(root.get("productNo"), productNo)
                ));
        if (comments.isEmpty()) {
            throw new BusinessException("评价不存在");
        }
        comments.forEach(c -> c.setDeleted(true));
        commentRepository.saveAll(comments);
    }
}
