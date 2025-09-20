package com.eon.user.repository;

import com.eon.user.entity.ApiResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiResourceRepository extends JpaRepository<ApiResource, Long> {
    List<ApiResource> findByTenantIdOrTenantIdIsNull(Long tenantId);
}