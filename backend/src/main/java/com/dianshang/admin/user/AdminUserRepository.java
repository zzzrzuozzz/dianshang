package com.dianshang.admin.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long>, JpaSpecificationExecutor<AdminUser> {

    Optional<AdminUser> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<AdminUser> findByPhone(String phone);

    Optional<AdminUser> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
