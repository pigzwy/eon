package com.eon.user.repository;

import com.eon.user.entity.UserRole;
import com.eon.user.entity.UserRoleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleKey> {
    List<UserRole> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    List<UserRole> findByRoleId(Long roleId);
}
