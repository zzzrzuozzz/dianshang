package com.dianshang.admin.member.repository;

import com.dianshang.admin.member.entity.MemberAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberAddressRepository extends JpaRepository<MemberAddressEntity, Long> {

    List<MemberAddressEntity> findByUserNoOrderByIsDefaultDesc(String userNo);
}
