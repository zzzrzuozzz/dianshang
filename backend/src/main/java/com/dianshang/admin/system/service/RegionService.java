package com.dianshang.admin.system.service;

import com.dianshang.admin.system.dto.RegionVO;
import com.dianshang.admin.system.entity.RegionEntity;
import com.dianshang.admin.system.repository.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class RegionService {

    private final RegionRepository regionRepository;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    /**
     * 按父级代码懒加载下级区划。parent_code 字段已建索引，单次查询目标 &lt; 10ms。
     */
    public List<RegionVO> listByParent(String parentCode) {
        String parent = StringUtils.hasText(parentCode) ? parentCode.trim() : "0";
        return regionRepository.findByParentCodeOrderByCodeAsc(parent).stream()
                .map(this::toVO)
                .toList();
    }

    public Optional<RegionEntity> findByCode(String code) {
        return regionRepository.findByCode(code);
    }

    public String resolveName(String code) {
        if (!StringUtils.hasText(code)) {
            return "";
        }
        return regionRepository.findByCode(code.trim())
                .map(RegionEntity::getName)
                .orElse("");
    }

    private RegionVO toVO(RegionEntity e) {
        RegionVO vo = new RegionVO();
        vo.setCode(e.getCode());
        vo.setName(e.getName());
        vo.setLevel(e.getLevelNum());
        return vo;
    }
}
