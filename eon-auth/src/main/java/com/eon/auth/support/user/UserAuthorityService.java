package com.eon.auth.support.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户角色与权限查询服务。
 *
 * <p>授权服务器在生成 JWT 时需要补充用户的角色与权限信息，
 * 该服务负责从业务数据库中读取最新的角色、权限快照，确保令牌中的声明字段真实可用。</p>
 */
@Service
public class UserAuthorityService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthorityService.class);

    /** JDBC 模板，用于执行轻量级查询 */
    private final JdbcTemplate jdbcTemplate;

    public UserAuthorityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 加载指定用户名的角色与权限快照。
     *
     * @param username 登录用户名
     * @return 查询结果，若用户不存在或未启用则返回空快照
     */
    public UserAuthoritySnapshot loadAuthoritySnapshot(String username) {
        if (username == null || username.isBlank()) {
            return UserAuthoritySnapshot.EMPTY;
        }

        UserIdentity identity = queryUserIdentity(username);
        if (identity == null) {
            log.debug("用户 [{}] 不存在或已被禁用，跳过角色权限查询", username);
            return UserAuthoritySnapshot.EMPTY;
        }
        List<String> roleCodes = queryRoleCodes(identity.userId());
        List<String> permissionCodes = queryPermissionCodes(identity.userId());

        return new UserAuthoritySnapshot(
                identity.userId(),
                identity.tenantId(),
                identity.policyVersion(),
                roleCodes,
                permissionCodes
        );
    }

    /**
     * 查询用户基础信息，过滤掉已停用账号。
     */
    private UserIdentity queryUserIdentity(String username) {
        List<UserIdentity> identities = jdbcTemplate.query(
                """
                        SELECT id, tenant_id, policy_version
                        FROM users
                        WHERE username = ? AND is_active = 1
                        ORDER BY id
                        LIMIT 1
                        """,
                (rs, rowNum) -> new UserIdentity(
                        rs.getLong("id"),
                        rs.getObject("tenant_id") != null ? rs.getLong("tenant_id") : null,
                        rs.getObject("policy_version") != null ? rs.getInt("policy_version") : null
                ),
                username
        );
        return identities.isEmpty() ? null : identities.get(0);
    }

    /**
     * 查询用户的全部角色编码，角色编码与授权服务器的 ROLE_ 前缀保持一致。
     */
    private List<String> queryRoleCodes(Long userId) {
        List<String> roles = jdbcTemplate.queryForList(
                """
                        SELECT DISTINCT r.code
                        FROM user_roles ur
                        JOIN roles r ON ur.role_id = r.id
                        WHERE ur.user_id = ?
                        ORDER BY r.code
                        """,
                String.class,
                userId
        );
        return roles.isEmpty() ? Collections.emptyList() : List.copyOf(roles);
    }
    /**
     * 查询用户聚合后的权限编码，格式为 resource_key:ACTION。
     */
    private List<String> queryPermissionCodes(Long userId) {
        List<String> permissions = jdbcTemplate.queryForList(
                """
                        SELECT DISTINCT CASE p.effect
                                WHEN 'DENY' THEN CONCAT('DENY:', p.resource_key, ':', p.action)
                                ELSE CONCAT(p.resource_key, ':', p.action)
                            END AS permission_code
                        FROM user_roles ur
                        JOIN role_permissions rp ON ur.role_id = rp.role_id
                        JOIN permissions p ON rp.permission_id = p.id
                        WHERE ur.user_id = ?
                        ORDER BY permission_code
                        """,
                String.class,
                userId
        );
        return permissions.isEmpty() ? Collections.emptyList() : List.copyOf(permissions);
    }

    /**
     * 角色与权限的不可变快照，方便在 JWT 自定义器中消费。
     */
    public record UserAuthoritySnapshot(Long userId,
                                        Long tenantId,
                                        Integer policyVersion,
                                        List<String> roles,
                                        List<String> permissions) {

        /** 共享的空快照常量，避免重复创建对象 */
        public static final UserAuthoritySnapshot EMPTY = new UserAuthoritySnapshot(null, null, null, Collections.emptyList(), Collections.emptyList());

        /**
         * 判断快照是否包含有效用户数据。
         */
        public boolean hasUser() {
            return userId != null;
        }
    }

    private record UserIdentity(Long userId, Long tenantId, Integer policyVersion) {
    }
}
