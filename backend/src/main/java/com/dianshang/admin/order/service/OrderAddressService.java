package com.dianshang.admin.order.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.order.dto.OrderAddressSaveRequest;
import com.dianshang.admin.order.dto.OrderAddressVO;
import com.dianshang.admin.order.entity.OrderAddressEntity;
import com.dianshang.admin.order.repository.OrderAddressRepository;
import com.dianshang.admin.system.service.RegionService;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderAddressService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");

    private final OrderAddressRepository orderAddressRepository;
    private final RegionService regionService;

    public OrderAddressService(OrderAddressRepository orderAddressRepository, RegionService regionService) {
        this.orderAddressRepository = orderAddressRepository;
        this.regionService = regionService;
    }

    public PageResult<OrderAddressVO> list(String type, String keyword, int page, int pageSize) {
        validateType(type);
        Specification<OrderAddressEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            predicates.add(cb.equal(root.get("addressType"), type));
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("contactName"), like),
                        cb.like(root.get("phone"), like),
                        cb.like(root.get("detailAddress"), like),
                        cb.like(root.get("addressCode"), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<OrderAddressEntity> result = orderAddressRepository.findAll(spec,
                PageRequest.of(Math.max(page - 1, 0), pageSize,
                        Sort.by(Sort.Direction.DESC, "sortNum", "id")));
        return new PageResult<>(result.getContent().stream().map(this::toVO).toList(),
                result.getTotalElements(), page, pageSize);
    }

    @Transactional
    public OrderAddressVO create(OrderAddressSaveRequest request) {
        validateType(request.getType());
        OrderAddressEntity entity = new OrderAddressEntity();
        entity.setAddressCode("AD" + System.currentTimeMillis() % 100000);
        entity.setAddressType(request.getType());
        applySave(entity, request);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultExcept(request.getType(), null);
            entity.setIsDefault(true);
        }
        return toVO(orderAddressRepository.save(entity));
    }

    @Transactional
    public OrderAddressVO update(String code, OrderAddressSaveRequest request) {
        OrderAddressEntity entity = require(code);
        if (!entity.getAddressType().equals(request.getType())) {
            throw new BusinessException("地址类型不可修改");
        }
        applySave(entity, request);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultExcept(entity.getAddressType(), entity.getId());
            entity.setIsDefault(true);
        }
        return toVO(orderAddressRepository.save(entity));
    }

    @Transactional
    public void delete(String code) {
        OrderAddressEntity entity = require(code);
        entity.setDeleted(true);
        orderAddressRepository.save(entity);
    }

    @Transactional
    public void updateVisible(String code, boolean visible) {
        OrderAddressEntity entity = require(code);
        entity.setVisible(visible);
        orderAddressRepository.save(entity);
    }

    @Transactional
    public void updateDefault(String code, boolean isDefault) {
        OrderAddressEntity entity = require(code);
        if (isDefault) {
            clearDefaultExcept(entity.getAddressType(), entity.getId());
            entity.setIsDefault(true);
            entity.setVisible(true);
        } else {
            entity.setIsDefault(false);
        }
        orderAddressRepository.save(entity);
    }

    public String resolveDefaultFullAddress(String type) {
        return findDefaultEntity(type).map(this::buildFullAddress).orElse(null);
    }

    public OrderAddressEntity findDefaultShipEntity() {
        return findDefaultEntity("ship").orElse(null);
    }

    private java.util.Optional<OrderAddressEntity> findDefaultEntity(String type) {
        return orderAddressRepository
                .findFirstByAddressTypeAndDeletedFalseAndIsDefaultTrueAndVisibleTrue(type);
    }

    private void applySave(OrderAddressEntity entity, OrderAddressSaveRequest request) {
        entity.setContactName(request.getContactName().trim());
        entity.setPhone(request.getPhone().trim());
        entity.setProvinceCode(trimOrEmpty(request.getProvinceCode()));
        entity.setCityCode(trimOrEmpty(request.getCityCode()));
        entity.setDistrictCode(trimOrEmpty(request.getDistrictCode()));
        String province = trimOrEmpty(request.getProvince());
        String city = trimOrEmpty(request.getCity());
        String district = trimOrEmpty(request.getDistrict());
        if (!StringUtils.hasText(province) && StringUtils.hasText(entity.getProvinceCode())) {
            province = regionService.resolveName(entity.getProvinceCode());
        }
        if (!StringUtils.hasText(city) && StringUtils.hasText(entity.getCityCode())) {
            city = regionService.resolveName(entity.getCityCode());
        }
        if (!StringUtils.hasText(district) && StringUtils.hasText(entity.getDistrictCode())) {
            district = regionService.resolveName(entity.getDistrictCode());
        }
        entity.setProvince(province);
        entity.setCity(city);
        entity.setDistrict(district);
        entity.setDetailAddress(request.getDetailAddress().trim());
        entity.setZipCode(trimOrEmpty(request.getZipCode()));
        if (request.getVisible() != null) {
            entity.setVisible(request.getVisible());
        }
        if (request.getSort() != null) {
            entity.setSortNum(request.getSort());
        }
    }

    private void clearDefaultExcept(String type, Long keepId) {
        orderAddressRepository.findAll((root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.equal(root.get("addressType"), type),
                cb.isTrue(root.get("isDefault"))
        )).forEach(e -> {
            if (keepId == null || !keepId.equals(e.getId())) {
                e.setIsDefault(false);
                orderAddressRepository.save(e);
            }
        });
    }

    private OrderAddressEntity require(String code) {
        return orderAddressRepository.findByAddressCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException("地址不存在"));
    }

    private OrderAddressVO toVO(OrderAddressEntity e) {
        OrderAddressVO vo = new OrderAddressVO();
        vo.setId(e.getAddressCode());
        vo.setContactName(e.getContactName());
        vo.setPhone(e.getPhone());
        vo.setProvince(e.getProvince());
        vo.setCity(e.getCity());
        vo.setDistrict(e.getDistrict());
        vo.setProvinceCode(e.getProvinceCode());
        vo.setCityCode(e.getCityCode());
        vo.setDistrictCode(e.getDistrictCode());
        vo.setDetailAddress(e.getDetailAddress());
        vo.setFullAddress(buildFullAddress(e));
        vo.setZipCode(e.getZipCode());
        vo.setIsDefault(e.getIsDefault());
        vo.setVisible(e.getVisible());
        vo.setSort(e.getSortNum());
        vo.setAddTime(e.getAddTime().format(FMT));
        return vo;
    }

    private String buildFullAddress(OrderAddressEntity e) {
        return Stream.of(e.getProvince(), e.getCity(), e.getDistrict(), e.getDetailAddress())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(""));
    }

    private static void validateType(String type) {
        if (!"ship".equals(type) && !"return".equals(type)) {
            throw new BusinessException("地址类型无效");
        }
    }

    private static String trimOrEmpty(String v) {
        return v != null ? v.trim() : "";
    }
}
