package com.eon.user.repository;

import com.eon.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByTenantIdOrTenantIdIsNull(Long tenantId);
}
