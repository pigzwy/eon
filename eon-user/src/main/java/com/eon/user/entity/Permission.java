package com.eon.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Column(name = "resource_key")
    private String resourceKey;
    
    private String action;
    
    @Enumerated(EnumType.STRING)
    private Effect effect;
    
    @Column(name = "condition_json")
    private String conditionJson;

    public enum Effect { ALLOW, DENY }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getResourceKey() { return resourceKey; }
    public void setResourceKey(String resourceKey) { this.resourceKey = resourceKey; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Effect getEffect() { return effect; }
    public void setEffect(Effect effect) { this.effect = effect; }

    public String getConditionJson() { return conditionJson; }
    public void setConditionJson(String conditionJson) { this.conditionJson = conditionJson; }
}