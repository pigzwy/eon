package com.eon.user.repository;

import com.eon.user.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByTenantIdOrTenantIdIsNull(Long tenantId);
}