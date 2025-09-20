package com.eon.user.service;

import com.eon.user.dto.AssignPermissionsRequest;
import com.eon.user.dto.CreateRoleRequest;
import com.eon.user.dto.CreateUserRequest;
import com.eon.user.dto.RoleResponse;
import com.eon.user.dto.UpdateUserRequest;
import com.eon.user.dto.UserResponse;
import com.eon.user.entity.Permission;
import com.eon.user.entity.Role;
import com.eon.user.entity.RolePermission;
import com.eon.user.entity.User;
import com.eon.user.entity.UserRole;
import com.eon.user.repository.PermissionRepository;
import com.eon.user.repository.RolePermissionRepository;
import com.eon.user.repository.RoleRepository;
import com.eon.user.repository.UserRepository;
import com.eon.user.repository.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserApplicationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final PolicyService policyService;

    public UserApplicationService(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  UserRoleRepository userRoleRepository,
                                  RolePermissionRepository rolePermissionRepository,
                                  PermissionRepository permissionRepository,
                                  PasswordEncoder passwordEncoder,
                                  PolicyService policyService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.policyService = policyService;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        return assembleUserResponse(user, true);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        return assembleUserResponse(user, true);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        userRepository.findByUsernameAndTenantId(request.getUsername(), request.getTenantId())
                .ifPresent(u -> { throw new DataIntegrityViolationException("用户名已存在"); });

        User user = new User();
        user.setTenantId(request.getTenantId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setPolicyVersion(1);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        user = userRepository.save(user);
        assignRoles(user.getId(), request.getRoleIds());
        policyService.evict(user.getId());
        return assembleUserResponse(user, true);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        boolean policyChanged = false;

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getActive() != null) {
            user.setIsActive(request.getActive());
            policyChanged = true;
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleIds() != null) {
            policyChanged = assignRoles(userId, request.getRoleIds()) || policyChanged;
        }
        if (policyChanged) {
            bumpPolicyVersion(user);
        }
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        policyService.evict(userId);
        return assembleUserResponse(user, true);
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        Role role = new Role();
        role.setTenantId(request.getTenantId());
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setIsSystem(Boolean.TRUE.equals(request.getSystem()));
        Role saved = roleRepository.save(role);

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            assignPermissions(saved.getId(), request.getPermissionIds());
        }
        return assembleRoleResponse(saved);
    }

    @Transactional
    public RoleResponse assignPermissions(Long roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("角色不存在"));
        List<UserRole> affectedUsers = userRoleRepository.findByRoleId(roleId);
        assignPermissions(roleId, request.getPermissionIds());
        if (!affectedUsers.isEmpty()) {
            Set<Long> userIds = affectedUsers.stream().map(UserRole::getUserId).collect(Collectors.toSet());
            bumpUsersPolicyVersion(userIds);
            userIds.forEach(policyService::evict);
        }
        policyService.evictByRole(roleId);
        return assembleRoleResponse(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles(Long tenantId) {
        List<Role> roles = roleRepository.findByTenantIdOrTenantIdIsNull(tenantId);
        return roles.stream().map(this::assembleRoleResponse).collect(Collectors.toList());
    }

    private boolean assignRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null) {
            return false;
        }
        List<UserRole> existing = userRoleRepository.findByUserId(userId);
        Set<Long> existingRoleIds = existing.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        Set<Long> targetIds = new HashSet<>(roleIds);
        if (existingRoleIds.equals(targetIds)) {
            return false;
        }
        userRoleRepository.deleteByUserId(userId);
        if (!targetIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(targetIds);
            if (roles.size() != targetIds.size()) {
                throw new EntityNotFoundException("部分角色不存在，无法完成分配");
            }
            List<UserRole> toSave = targetIds.stream().map(roleId -> {
                UserRole relation = new UserRole();
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                return relation;
            }).toList();
            userRoleRepository.saveAll(toSave);
        }
        return true;
    }

    private void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionRepository.deleteByRoleId(roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new EntityNotFoundException("存在无效的权限 ID");
        }
        List<RolePermission> relations = permissionIds.stream().map(pid -> {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(pid);
            return rp;
        }).toList();
        rolePermissionRepository.saveAll(relations);
    }

    private void bumpPolicyVersion(User user) {
        Integer current = user.getPolicyVersion();
        if (current == null) {
            user.setPolicyVersion(1);
        } else {
            user.setPolicyVersion(current + 1);
        }
    }

    private void bumpUsersPolicyVersion(Set<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(this::bumpPolicyVersion);
        userRepository.saveAll(users);
    }

    private UserResponse assembleUserResponse(User user, boolean includePermissions) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setTenantId(user.getTenantId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setActive(user.getIsActive());
        response.setPolicyVersion(user.getPolicyVersion());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        List<UserRole> relations = userRoleRepository.findByUserId(user.getId());
        List<Long> roleIds = relations.stream().map(UserRole::getRoleId).toList();
        List<Role> roles = roleIds.isEmpty() ? Collections.emptyList() : roleRepository.findAllById(roleIds);
        response.setRoles(roles.stream().map(Role::getCode).sorted().toList());

        if (includePermissions) {
            List<String> permissions = resolvePermissions(roleIds);
            response.setPermissions(permissions);
        } else {
            response.setPermissions(Collections.emptyList());
        }
        return response;
    }

    private RoleResponse assembleRoleResponse(Role role) {
        RoleResponse resp = new RoleResponse();
        resp.setId(role.getId());
        resp.setTenantId(role.getTenantId());
        resp.setCode(role.getCode());
        resp.setName(role.getName());
        resp.setSystem(role.getIsSystem());
        List<RolePermission> relations = rolePermissionRepository.findByRoleId(role.getId());
        if (relations.isEmpty()) {
            resp.setPermissions(List.of());
            return resp;
        }
        List<Long> permissionIds = relations.stream().map(RolePermission::getPermissionId).toList();
        Map<Long, Permission> permissionMap = permissionRepository.findAllById(permissionIds).stream()
                .collect(Collectors.toMap(Permission::getId, Function.identity()));
        List<String> permissions = relations.stream()
                .map(rel -> permissionMap.get(rel.getPermissionId()))
                .filter(Objects::nonNull)
                .map(Permission::getResourceKey)
                .distinct()
                .sorted()
                .toList();
        resp.setPermissions(permissions);
        return resp;
    }

    private List<String> resolvePermissions(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        List<RolePermission> relations = rolePermissionRepository.findByRoleIdIn(roleIds);
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> permissionIds = relations.stream().map(RolePermission::getPermissionId).toList();
        return permissionRepository.findAllById(permissionIds).stream()
                .map(Permission::getResourceKey)
                .distinct()
                .sorted()
                .toList();
    }
}
