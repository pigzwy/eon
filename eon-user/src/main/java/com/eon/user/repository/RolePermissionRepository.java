package com.eon.user.repository;

import com.eon.user.entity.RolePermission;
import com.eon.user.entity.RolePermissionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionKey> {
    List<RolePermission> findByRoleIdIn(Iterable<Long> roleIds);
    List<RolePermission> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
}
