package com.dianshang.admin.ops.search;

import com.dianshang.admin.member.entity.MemberEntity;
import com.dianshang.admin.member.repository.MemberRepository;
import com.dianshang.admin.ops.search.dto.GlobalSearchItemVO;
import com.dianshang.admin.ops.search.dto.GlobalSearchResultVO;
import com.dianshang.admin.order.entity.OrderEntity;
import com.dianshang.admin.order.repository.OrderRepository;
import com.dianshang.admin.product.entity.Product;
import com.dianshang.admin.product.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GlobalSearchService {

    private static final int LIMIT = 5;

    private static final Map<String, String> ORDER_STATUS_LABEL = Map.of(
            "pending_payment", "待付款",
            "pending_ship", "待发货",
            "shipped", "已发货",
            "completed", "已完成",
            "closed", "已关闭"
    );

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    public GlobalSearchService(ProductRepository productRepository,
                               OrderRepository orderRepository,
                               MemberRepository memberRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
    }

    public GlobalSearchResultVO search(String keyword) {
        GlobalSearchResultVO vo = new GlobalSearchResultVO();
        if (!StringUtils.hasText(keyword)) {
            return vo;
        }
        String kw = keyword.trim();
        vo.setProducts(searchProducts(kw));
        vo.setOrders(searchOrders(kw));
        vo.setUsers(searchUsers(kw));
        return vo;
    }

    private List<GlobalSearchItemVO> searchProducts(String kw) {
        Specification<Product> spec = (root, query, cb) -> {
            String like = "%" + kw.toLowerCase() + "%";
            return cb.and(
                    cb.isFalse(root.get("deleted")),
                    cb.or(
                            cb.like(cb.lower(root.get("title")), like),
                            cb.like(cb.lower(root.get("productNo")), like),
                            cb.like(cb.lower(root.get("sku")), like)
                    )
            );
        };
        return productRepository.findAll(spec, PageRequest.of(0, LIMIT, Sort.by(Sort.Direction.DESC, "id")))
                .stream()
                .map(this::toProductItem)
                .toList();
    }

    private List<GlobalSearchItemVO> searchOrders(String kw) {
        Specification<OrderEntity> spec = (root, query, cb) -> {
            String like = "%" + kw.toLowerCase() + "%";
            return cb.and(
                    cb.isFalse(root.get("deleted")),
                    cb.or(
                            cb.like(cb.lower(root.get("orderNo")), like),
                            cb.like(cb.lower(root.get("productName")), like),
                            cb.like(cb.lower(root.get("receiverPhone")), like),
                            cb.like(cb.lower(root.get("receiverName")), like)
                    )
            );
        };
        return orderRepository.findAll(spec, PageRequest.of(0, LIMIT, Sort.by(Sort.Direction.DESC, "createTime")))
                .stream()
                .map(this::toOrderItem)
                .toList();
    }

    private List<GlobalSearchItemVO> searchUsers(String kw) {
        Specification<MemberEntity> spec = (root, query, cb) -> {
            String like = "%" + kw.toLowerCase() + "%";
            List<jakarta.persistence.criteria.Predicate> ors = new ArrayList<>();
            ors.add(cb.like(cb.lower(root.get("phone")), like));
            ors.add(cb.like(cb.lower(root.get("userNo")), like));
            ors.add(cb.like(cb.lower(root.get("nickname")), like));
            return cb.and(cb.isFalse(root.get("deleted")), cb.or(ors.toArray(new jakarta.persistence.criteria.Predicate[0])));
        };
        return memberRepository.findAll(spec, PageRequest.of(0, LIMIT, Sort.by(Sort.Direction.DESC, "id")))
                .stream()
                .map(this::toUserItem)
                .toList();
    }

    private GlobalSearchItemVO toProductItem(Product p) {
        GlobalSearchItemVO item = new GlobalSearchItemVO();
        item.setId(p.getProductNo());
        item.setName(p.getTitle());
        item.setTargetUrl("/product/add?id=" + p.getProductNo());
        return item;
    }

    private GlobalSearchItemVO toOrderItem(OrderEntity o) {
        GlobalSearchItemVO item = new GlobalSearchItemVO();
        item.setId(o.getOrderNo());
        String status = ORDER_STATUS_LABEL.getOrDefault(o.getOrderStatus(), o.getOrderStatus());
        item.setName(o.getProductName() + " · " + status);
        item.setTargetUrl("/order/detail/" + o.getOrderNo());
        return item;
    }

    private GlobalSearchItemVO toUserItem(MemberEntity m) {
        GlobalSearchItemVO item = new GlobalSearchItemVO();
        item.setId(m.getUserNo());
        String name = StringUtils.hasText(m.getNickname()) ? m.getNickname() : m.getPhone();
        item.setName(name);
        item.setTargetUrl("/user/detail/" + m.getUserNo());
        return item;
    }
}
