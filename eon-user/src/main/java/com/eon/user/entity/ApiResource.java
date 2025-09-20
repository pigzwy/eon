package com.eon.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "apis")
public class ApiResource {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;
    
    private String name;
    
    private String method;
    
    @Column(name = "path_template")
    private String pathTemplate;
    
    @Column(name = "path_regex")
    private String pathRegex;
    
    @Column(name = "is_public")
    private Boolean isPublic;
    
    @Column(name = "permission_key")
    private String permissionKey;
    
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPathTemplate() { return pathTemplate; }
    public void setPathTemplate(String pathTemplate) { this.pathTemplate = pathTemplate; }

    public String getPathRegex() { return pathRegex; }
    public void setPathRegex(String pathRegex) { this.pathRegex = pathRegex; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public String getPermissionKey() { return permissionKey; }
    public void setPermissionKey(String permissionKey) { this.permissionKey = permissionKey; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}