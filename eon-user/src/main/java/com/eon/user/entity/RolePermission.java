package com.eon.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionKey.class)
public class RolePermission {
    @Id 
    @Column(name = "role_id")
    private Long roleId;
    
    @Id 
    @Column(name = "permission_id")
    private Long permissionId;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
}