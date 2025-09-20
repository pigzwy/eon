-- 示例数据
INSERT IGNORE INTO roles (tenant_id, code, name, is_system) VALUES (NULL, 'admin', '系统管理员', 1);
INSERT IGNORE INTO users (tenant_id, username, email, password_hash)
VALUES (NULL, 'admin', 'admin@example.com', '$2a$10$Q8cSx2k3n1y2XhM6X0mO0.1bE3Yw8cP6o2p5n3k5yQx1Z0Vg9qUe2'); -- 明文 admin123（BCrypt）

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='admin' AND r.code='admin';

-- 菜单
INSERT IGNORE INTO menus (permission_key, title, route_path, sort_weight) VALUES
('menu:system.users', '用户管理', '/system/users', 10),
('menu:system.roles', '角色管理', '/system/roles', 20);

-- 对应权限
INSERT IGNORE INTO permissions (resource_key, action, effect) VALUES
('menu:system.users', 'view', 'ALLOW'),
('menu:system.roles', 'view', 'ALLOW');

-- API 资源（创建时服务会自动编译正则 & 生成 permission_key；以下手写示意）
INSERT IGNORE INTO apis (name, method, path_template, path_regex, is_public, permission_key)
VALUES
('获取用户', 'GET', '/users/:id', '^/users/[^/]+$', 0, 'api:GET:/users/:id'),
('创建用户', 'POST', '/users', '^/users$', 0, 'api:POST:/users');

-- API 权限
INSERT IGNORE INTO permissions (resource_key, action, effect) VALUES
('api:GET:/users/:id', 'invoke', 'ALLOW'),
('api:POST:/users', 'invoke', 'ALLOW');

-- 绑定到 admin 角色
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code='admin' AND p.resource_key IN ('menu:system.users','menu:system.roles','api:GET:/users/:id','api:POST:/users');