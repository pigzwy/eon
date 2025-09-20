package com.eon.user.service;

import com.eon.user.entity.*;
import com.eon.user.policy.CompiledPolicy;
import com.eon.user.repository.*;
import com.eon.user.util.PathPatternCompiler;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PolicyService {

    private final UserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final RolePermissionRepository rolePermRepo;
    private final PermissionRepository permRepo;
    private final ApiResourceRepository apiRepo;

    // 简化：本地内存缓存（可替换为 Caffeine/Redis）
    private final Map<Long, CompiledPolicy> cache = new ConcurrentHashMap<>();

    public PolicyService(UserRepository userRepo,
                        UserRoleRepository userRoleRepo,
                        RolePermissionRepository rolePermRepo,
                        PermissionRepository permRepo,
                        ApiResourceRepository apiRepo) {
        this.userRepo = userRepo;
        this.userRoleRepo = userRoleRepo;
        this.rolePermRepo = rolePermRepo;
        this.permRepo = permRepo;
        this.apiRepo = apiRepo;
    }

    public CompiledPolicy getPolicy(Long userId, int tokenPv) {
        CompiledPolicy p = cache.get(userId);
        Integer dbPv = userRepo.findById(userId).map(User::getPolicyVersion).orElse(0);
        boolean needRebuild = (p == null) || !Objects.equals(p.getPolicyVersion(), dbPv) || tokenPv != dbPv;
        if (needRebuild) {
            p = rebuild(userId);
            cache.put(userId, p);
        }
        return p;
    }

    public void evict(Long userId) {
        cache.remove(userId);
    }

    public void evictByRole(Long roleId) {
        userRoleRepo.findByRoleId(roleId).stream()
                .map(UserRole::getUserId)
                .distinct()
                .forEach(cache::remove);
    }

    private CompiledPolicy rebuild(Long userId) {
        User u = userRepo.findById(userId).orElseThrow();
        List<Long> roleIds = userRoleRepo.findByUserId(userId).stream()
                .map(UserRole::getRoleId).collect(Collectors.toList());

        // 角色拥有的 permissionId
        List<Long> permIds = rolePermRepo.findByRoleIdIn(roleIds).stream()
                .map(RolePermission::getPermissionId).collect(Collectors.toList());

        Map<String, String> menuEffects = new HashMap<>();
        List<CompiledPolicy.ApiRule> apiRules = new ArrayList<>();

        Map<Long, Permission> permMap = permRepo.findAllById(permIds).stream()
                .collect(Collectors.toMap(Permission::getId, x -> x));

        // 预取所有 API 资源（含公共）
        List<ApiResource> apis = apiRepo.findByTenantIdOrTenantIdIsNull(u.getTenantId());

        for (Permission p : permMap.values()) {
            String key = p.getResourceKey();
            String effect = p.getEffect().name();

            if (key.startsWith("menu:")) {
                // DENY 优先：若已有 ALLOW，再写 DENY 会覆盖
                menuEffects.put(key, effect);
            } else if (key.startsWith("api:")) {
                // 从权限键解析 method + template（也可通过 apis 表再反查更稳妥）
                String[] parts = key.split(":", 3); // api, METHOD, /path
                String method = parts.length > 1 ? parts[1] : "*";
                String pathTemplate = parts.length > 2 ? parts[2] : "/**";

                // 匹配表里的编译正则（同 path_template 的）
                String regex = apis.stream()
                        .filter(a -> a.getPermissionKey().equals(key))
                        .map(ApiResource::getPathRegex)
                        .findFirst()
                        .orElseGet(() -> PathPatternCompiler.toRegex(pathTemplate));

                CompiledPolicy.ApiRule rule = new CompiledPolicy.ApiRule();
                rule.setKey(key);
                rule.setMethod(method.toUpperCase());
                rule.setRegex(Pattern.compile(regex));
                rule.setEffect(effect);
                apiRules.add(rule);
            }
        }

        CompiledPolicy policy = new CompiledPolicy();
        policy.setUserId(userId);
        policy.setPolicyVersion(u.getPolicyVersion());
        policy.setMenuEffects(menuEffects);
        policy.setApiRules(apiRules);
        return policy;
    }
}
