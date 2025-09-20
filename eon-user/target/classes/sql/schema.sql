-- 可选：租户
CREATE TABLE IF NOT EXISTS tenants (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  code         VARCHAR(128) NOT NULL UNIQUE,
  name         VARCHAR(255) NOT NULL,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 用户
CREATE TABLE IF NOT EXISTS users (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id       BIGINT NULL,
  username        VARCHAR(128) NOT NULL,
  email           VARCHAR(255),
  password_hash   VARCHAR(255) NOT NULL,
  is_active       TINYINT(1) DEFAULT 1,
  policy_version  INT DEFAULT 1,   -- 权限变更版本（用于缓存刷新）
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_user_tenant_username (tenant_id, username),
  CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色
CREATE TABLE IF NOT EXISTS roles (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id    BIGINT NULL,
  code         VARCHAR(128) NOT NULL,
  name         VARCHAR(255) NOT NULL,
  is_system    TINYINT(1) DEFAULT 0,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_role_tenant_code (tenant_id, code),
  CONSTRAINT fk_roles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户-角色
CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 菜单（树）
CREATE TABLE IF NOT EXISTS menus (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id      BIGINT NULL,
  parent_id      BIGINT NULL,
  title          VARCHAR(255) NOT NULL,
  route_path     VARCHAR(255) NOT NULL,
  component      VARCHAR(255),
  icon           VARCHAR(128),
  sort_weight    INT DEFAULT 100,
  visible        TINYINT(1) DEFAULT 1,
  permission_key VARCHAR(255) NOT NULL,        -- 例：menu:system.users
  meta_json      JSON,
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_menu_perm (tenant_id, permission_key),
  KEY idx_menus_parent (parent_id),
  CONSTRAINT fk_menus_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  CONSTRAINT fk_menus_parent FOREIGN KEY (parent_id) REFERENCES menus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- API 资源
CREATE TABLE IF NOT EXISTS apis (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id      BIGINT NULL,
  name           VARCHAR(255) NOT NULL,
  method         VARCHAR(16) NOT NULL,         -- GET/POST/PUT/DELETE/*
  path_template  VARCHAR(255) NOT NULL,        -- /users/:id, /orders/**
  path_regex     VARCHAR(512) NOT NULL,        -- 预编译正则，如 ^/users/[^/]+$
  is_public      TINYINT(1) DEFAULT 0,
  permission_key VARCHAR(255) NOT NULL,        -- 例：api:GET:/users/:id
  description    VARCHAR(512),
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_api_perm (tenant_id, permission_key),
  KEY idx_apis_match (method, path_regex),
  CONSTRAINT fk_apis_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API资源表';

-- 权限定义（与资源解耦，用 resource_key 指向 menus.permission_key 或 apis.permission_key）
CREATE TABLE IF NOT EXISTS permissions (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id      BIGINT NULL,
  resource_key   VARCHAR(255) NOT NULL,        -- menu:* 或 api:*
  action         VARCHAR(64)  NOT NULL,        -- 菜单= view；API= invoke / create / read / update / delete
  effect         ENUM('ALLOW','DENY') NOT NULL DEFAULT 'ALLOW',
  condition_json JSON NULL,                    -- ABAC 扩展（可为空）
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_perm (tenant_id, resource_key, action, effect),
  CONSTRAINT fk_perms_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色-权限
CREATE TABLE IF NOT EXISTS role_permissions (
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_rp_role       FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 审计（可选）
CREATE TABLE IF NOT EXISTS audit_logs (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT,
  action      VARCHAR(64) NOT NULL,
  detail_json JSON,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  KEY idx_audit_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';