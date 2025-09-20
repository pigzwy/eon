-- ===================================================================
-- EON 授权服务数据库表结构定义
-- ===================================================================
-- 用于OAuth2授权服务器和用户权限管理的数据表结构
-- 支持MySQL和H2数据库，提供完整的OAuth2标准表结构
-- 
-- 主要表组织：
-- 1. OAuth2标准表 - 符合Spring Security OAuth2规范
-- 2. 用户权限表 - RBAC权限模型实现  
-- 3. 多租户支持 - 通过tenant_id字段实现数据隔离
-- ===================================================================

-- ===================================================================
-- OAuth2 标准表结构
-- ===================================================================

-- OAuth2 注册客户端表
-- 存储OAuth2客户端应用的注册信息，支持多种授权模式
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
      id varchar(100) NOT NULL,
      client_id varchar(100) NOT NULL,
      client_id_issued_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
      client_secret varchar(200) DEFAULT NULL,
      client_secret_expires_at timestamp DEFAULT NULL,
      client_name varchar(200) NOT NULL,
      client_authentication_methods varchar(1000) NOT NULL,
      authorization_grant_types varchar(1000) NOT NULL,
      redirect_uris varchar(1000) DEFAULT NULL,
      post_logout_redirect_uris varchar(1000) DEFAULT NULL,
      scopes varchar(1000) NOT NULL,
      client_settings varchar(2000) NOT NULL,
      token_settings varchar(2000) NOT NULL,
      PRIMARY KEY (id)
);

-- OAuth2 授权信息表
-- 存储OAuth2授权过程中的状态信息和令牌数据
/*
IMPORTANT:
    If using PostgreSQL, update ALL columns defined with 'blob' to 'text',
    as PostgreSQL does not support the 'blob' data type.
*/
CREATE TABLE IF NOT EXISTS oauth2_authorization (
      id varchar(100) NOT NULL,
      registered_client_id varchar(100) NOT NULL,
      principal_name varchar(200) NOT NULL,
      authorization_grant_type varchar(100) NOT NULL,
      authorized_scopes varchar(1000) DEFAULT NULL,
      attributes blob DEFAULT NULL,
      state varchar(500) DEFAULT NULL,
      authorization_code_value blob DEFAULT NULL,
      authorization_code_issued_at timestamp DEFAULT NULL,
      authorization_code_expires_at timestamp DEFAULT NULL,
      authorization_code_metadata blob DEFAULT NULL,
      access_token_value blob DEFAULT NULL,
      access_token_issued_at timestamp DEFAULT NULL,
      access_token_expires_at timestamp DEFAULT NULL,
      access_token_metadata blob DEFAULT NULL,
      access_token_type varchar(100) DEFAULT NULL,
      access_token_scopes varchar(1000) DEFAULT NULL,
      oidc_id_token_value blob DEFAULT NULL,
      oidc_id_token_issued_at timestamp DEFAULT NULL,
      oidc_id_token_expires_at timestamp DEFAULT NULL,
      oidc_id_token_metadata blob DEFAULT NULL,
      refresh_token_value blob DEFAULT NULL,
      refresh_token_issued_at timestamp DEFAULT NULL,
      refresh_token_expires_at timestamp DEFAULT NULL,
      refresh_token_metadata blob DEFAULT NULL,
      user_code_value blob DEFAULT NULL,
      user_code_issued_at timestamp DEFAULT NULL,
      user_code_expires_at timestamp DEFAULT NULL,
      user_code_metadata blob DEFAULT NULL,
      device_code_value blob DEFAULT NULL,
      device_code_issued_at timestamp DEFAULT NULL,
      device_code_expires_at timestamp DEFAULT NULL,
      device_code_metadata blob DEFAULT NULL,
      PRIMARY KEY (id)
);

-- OAuth2 授权同意表  
-- 存储用户对特定客户端和权限范围的授权同意记录
CREATE TABLE IF NOT EXISTS oauth2_authorization_consent (
      registered_client_id varchar(100) NOT NULL,
      principal_name varchar(200) NOT NULL,
      authorities varchar(1000) NOT NULL,
      PRIMARY KEY (registered_client_id, principal_name)
);

-- ===================================================================
-- 用户权限管理表结构（RBAC模型）
-- ===================================================================

-- 用户表
-- 存储用户基本信息，支持多租户架构
CREATE TABLE IF NOT EXISTS users (
    -- 用户主键ID
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 租户ID（多租户支持，NULL表示系统级用户）
    tenant_id BIGINT NULL,
    -- 用户名（在租户范围内唯一）
    username VARCHAR(64) NOT NULL,
    -- 邮箱地址
    email VARCHAR(128),
    -- 密码哈希值（BCrypt加密）
    password_hash VARCHAR(200) NOT NULL,
    -- 账户状态（1=激活，0=禁用）
    is_active TINYINT(1) DEFAULT 1,
    -- 隐私政策版本（合规管理）
    policy_version INT DEFAULT 1,
    -- 创建时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 更新时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 唯一约束：租户内用户名唯一
    CONSTRAINT uq_user_tenant_username UNIQUE (tenant_id, username)
);

-- 角色表
-- 定义系统中的各种角色
CREATE TABLE IF NOT EXISTS roles (
    -- 角色主键ID  
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 租户ID（多租户支持）
    tenant_id BIGINT NULL,
    -- 角色代码（程序中使用的标识）
    code VARCHAR(128) NOT NULL,
    -- 角色显示名称
    name VARCHAR(255) NOT NULL,
    -- 是否系统角色（1=系统角色不可删除，0=普通角色）
    is_system TINYINT(1) DEFAULT 0,
    -- 创建时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 更新时间  
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 唯一约束：租户内角色代码唯一
    CONSTRAINT uq_role_tenant_code UNIQUE (tenant_id, code)
);

-- 用户角色关联表
-- 实现用户和角色的多对多关联
CREATE TABLE IF NOT EXISTS user_roles (
    -- 用户ID（外键关联users表）
    user_id BIGINT NOT NULL,
    -- 角色ID（外键关联roles表）
    role_id BIGINT NOT NULL,
    -- 复合主键：确保用户角色关联的唯一性
    PRIMARY KEY (user_id, role_id)
);

-- 权限表  
-- 定义系统中的各种权限，支持细粒度权限控制
CREATE TABLE IF NOT EXISTS permissions (
    -- 权限主键ID
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 租户ID（多租户支持）
    tenant_id BIGINT NULL,
    -- 资源键（如API路径、功能模块等）
    resource_key VARCHAR(255) NOT NULL,
    -- 操作类型（READ, WRITE, DELETE, EXECUTE等）
    action VARCHAR(64) NOT NULL,
    -- 权限效果（ALLOW=允许，DENY=拒绝）
    effect VARCHAR(16) NOT NULL DEFAULT 'ALLOW'
);

-- 角色权限关联表
-- 实现角色和权限的多对多关联
CREATE TABLE IF NOT EXISTS role_permissions (
    -- 角色ID（外键关联roles表）
    role_id BIGINT NOT NULL,
    -- 权限ID（外键关联permissions表）
    permission_id BIGINT NOT NULL,
    -- 复合主键：确保角色权限关联的唯一性
    PRIMARY KEY (role_id, permission_id)
);

-- ===================================================================
-- 索引优化
-- ===================================================================

-- -- OAuth2相关表的索引
-- CREATE INDEX IF NOT EXISTS INDEX idx_oauth2_authorization_client_id ON oauth2_authorization (registered_client_id);
-- CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_principal ON oauth2_authorization(principal_name);
-- CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_grant_type ON oauth2_authorization(authorization_grant_type);
--
-- -- 用户权限相关表的索引
-- CREATE INDEX IF NOT EXISTS idx_users_tenant_username ON users(tenant_id, username);
-- CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
-- CREATE INDEX IF NOT EXISTS idx_roles_tenant_code ON roles(tenant_id, code);
-- CREATE INDEX IF NOT EXISTS idx_permissions_resource ON permissions(resource_key);
-- CREATE INDEX IF NOT EXISTS idx_permissions_tenant ON permissions(tenant_id);

-- ===================================================================
-- 数据约束和外键（可选，根据数据库支持情况启用）
-- ===================================================================

-- 外键约束（H2数据库可能不完全支持，MySQL支持）
-- ALTER TABLE oauth2_authorization ADD CONSTRAINT fk_auth_client 
--     FOREIGN KEY (registered_client_id) REFERENCES oauth2_registered_client(id);
-- ALTER TABLE oauth2_authorization_consent ADD CONSTRAINT fk_consent_client
--     FOREIGN KEY (registered_client_id) REFERENCES oauth2_registered_client(id);
-- ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user  
--     FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_role
--     FOREIGN KEY (role_id) REFERENCES roles(id);
-- ALTER TABLE role_permissions ADD CONSTRAINT fk_role_permissions_role
--     FOREIGN KEY (role_id) REFERENCES roles(id);
-- ALTER TABLE role_permissions ADD CONSTRAINT fk_role_permissions_permission  
--     FOREIGN KEY (permission_id) REFERENCES permissions(id);
