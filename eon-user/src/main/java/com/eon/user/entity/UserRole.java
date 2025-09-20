package com.eon.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_roles")
@IdClass(UserRoleKey.class)
public class UserRole {
    @Id 
    @Column(name = "user_id")
    private Long userId;
    
    @Id 
    @Column(name = "role_id")
    private Long roleId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}