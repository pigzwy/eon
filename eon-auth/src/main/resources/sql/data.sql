-- ===================================================================
-- EON 授权服务初始化数据
-- ===================================================================  
-- 用于OAuth2授权服务器和用户权限管理的初始化数据
-- 
-- 数据内容：
-- 1. 默认OAuth2客户端 - 用于演示和测试
-- 2. 系统默认用户 - 管理员账户
-- 3. 基础角色权限 - RBAC权限体系
-- 4. 演示权限数据 - 常用权限配置
-- ===================================================================

-- ===================================================================
-- 系统基础数据
-- ===================================================================

-- 默认系统管理员用户
-- 密码：admin123 (BCrypt加密)
INSERT IGNORE INTO users (tenant_id, username, email, password_hash, is_active, policy_version, created_at, updated_at)
VALUES 
    (NULL, 'admin', 'admin@eon.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyF5bNw6rojUxJZpx.Cpky3Jy5C', 1, 1, NOW(), NOW()),
    (NULL, 'demo', 'demo@eon.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyF5bNw6rojUxJZpx.Cpky3Jy5C', 1, 1, NOW(), NOW());

-- 系统默认角色
INSERT IGNORE INTO roles (tenant_id, code, name, is_system, created_at, updated_at)
VALUES 
    (NULL, 'ADMIN', '系统管理员', 1, NOW(), NOW()),
    (NULL, 'USER', '普通用户', 1, NOW(), NOW()),
    (NULL, 'GUEST', '访客用户', 1, NOW(), NOW());

-- 基础权限定义
INSERT IGNORE INTO permissions (tenant_id, resource_key, action, effect)
VALUES 
    -- 用户管理权限
    (NULL, '/api/users', 'READ', 'ALLOW'),
    (NULL, '/api/users', 'WRITE', 'ALLOW'),
    (NULL, '/api/users', 'DELETE', 'ALLOW'),
    
    -- 角色管理权限  
    (NULL, '/api/roles', 'READ', 'ALLOW'),
    (NULL, '/api/roles', 'WRITE', 'ALLOW'),
    (NULL, '/api/roles', 'DELETE', 'ALLOW'),
    
    -- 权限管理权限
    (NULL, '/api/permissions', 'READ', 'ALLOW'),
    (NULL, '/api/permissions', 'WRITE', 'ALLOW'),
    
    -- 系统监控权限
    (NULL, '/actuator/**', 'READ', 'ALLOW'),
    
    -- OAuth2管理权限
    (NULL, '/api/oauth2/**', 'READ', 'ALLOW'),
    (NULL, '/api/oauth2/**', 'WRITE', 'ALLOW');

-- 用户角色关联（管理员分配所有角色）
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.code = 'ADMIN';

INSERT IGNORE INTO user_roles (user_id, role_id)  
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'demo' AND r.code = 'USER';

-- 角色权限关联（管理员角色拥有所有权限）
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p 
WHERE r.code = 'ADMIN';

-- 普通用户角色权限（只读权限）
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'USER' AND p.action = 'READ';

-- ===================================================================
-- OAuth2客户端数据（由程序在启动时自动创建，这里仅作注释说明）
-- ===================================================================

-- 默认OAuth2客户端会在AuthorizationServerConfig中自动注册：
-- 客户端ID: eon-console
-- 客户端密钥: console-secret
-- 授权模式: authorization_code, refresh_token, client_credentials
-- 重定向URI: http://127.0.0.1:8080/login/oauth2/code/eon
-- 权限范围: openid, profile

-- 如需手动插入客户端数据，可参考以下格式：
/*INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_secret_expires_at,
    client_name, client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
) VALUES (
    'eon-console-id',
    'eon-console', 
    NOW(),
    '$2a$10$vKDhfE.6W1zLZdgE1L5nF.k3LF5JGr5tJ1R7h1YsG.x2U4QWdTQ9O',  -- BCrypt编码的密钥
    NULL,
    'EON Console Application',
    'client_secret_basic',
    'authorization_code,refresh_token,client_credentials,password',
    'http://127.0.0.1:8080/login/oauth2/code/eon',
    'http://127.0.0.1:8080',
    'openid,profile,read,write',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":true,"settings.client.require-authorization-consent":true}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.refresh-token-time-to-live":["java.time.Duration",604800.000000000],"settings.token.reuse-refresh-tokens":false}'
) ON DUPLICATE KEY UPDATE
    client_secret = VALUES(client_secret),
    authorization_grant_types = VALUES(authorization_grant_types),
    redirect_uris = VALUES(redirect_uris),
    scopes = VALUES(scopes);*/

-- ===================================================================
-- 测试和演示数据（可选）
-- ===================================================================

-- 多租户演示数据
INSERT IGNORE INTO users (tenant_id, username, email, password_hash, is_active, policy_version, created_at, updated_at)
VALUES 
    (1, 'tenant1_admin', 'admin@tenant1.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyF5bNw6rojUxJZpx.Cpky3Jy5C', 1, 1, NOW(), NOW()),
    (2, 'tenant2_admin', 'admin@tenant2.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyF5bNw6rojUxJZpx.Cpky3Jy5C', 1, 1, NOW(), NOW());

-- 租户专用角色  
INSERT IGNORE INTO roles (tenant_id, code, name, is_system, created_at, updated_at)
VALUES 
    (1, 'TENANT_ADMIN', '租户1管理员', 0, NOW(), NOW()),
    (2, 'TENANT_ADMIN', '租户2管理员', 0, NOW(), NOW());

-- 占位查询（确保脚本可以正常执行）
SELECT 1 as placeholder;