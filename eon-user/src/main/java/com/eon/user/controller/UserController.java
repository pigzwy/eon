package com.eon.user.controller;

import com.eon.common.security.context.AuthenticatedUser;
import com.eon.common.security.context.CurrentUser;
import com.eon.user.dto.CreateUserRequest;
import com.eon.user.dto.UpdateUserRequest;
import com.eon.user.dto.UserMeResponse;
import com.eon.user.dto.UserResponse;
import com.eon.user.policy.CompiledPolicy;
import com.eon.user.service.MenuService;
import com.eon.user.service.PolicyService;
import com.eon.user.service.UserApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserApplicationService userApplicationService;
    private final PolicyService policyService;
    private final MenuService menuService;

    public UserController(UserApplicationService userApplicationService,
                          PolicyService policyService,
                          MenuService menuService) {
        this.userApplicationService = userApplicationService;
        this.policyService = policyService;
        this.menuService = menuService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(@CurrentUser AuthenticatedUser currentUser) {
        try {
            // 验证用户信息是否有效
            if (currentUser == null || currentUser.userId() == null) {
                log.warn("用户认证信息缺失");
                return ResponseEntity.badRequest().build();
            }
            
            Long userId = currentUser.userId();
            log.debug("获取用户信息，用户ID: {}", userId);
            
            // 获取用户基础信息
            UserResponse profile = userApplicationService.getUserProfile(userId);
            if (profile == null) {
                log.warn("用户不存在，用户ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
            
            // 确定策略版本：优先使用用户上下文中的版本，否则使用用户档案中的版本
            int policyVersion = currentUser.policyVersion() != null
                    ? currentUser.policyVersion()
                    : profile.getPolicyVersion();
            
            // 获取权限策略和菜单
            CompiledPolicy policy = policyService.getPolicy(userId, policyVersion);
            List<MenuService.MenuNode> menus = menuService.userMenus(userId, currentUser.tenantId(), policy);
            
            // 构建响应对象
            UserMeResponse response = new UserMeResponse();
            response.setProfile(profile);
            response.setMenus(menus);
            
            log.debug("用户信息获取成功，用户ID: {}", userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable("id") Long userId) {
        return userApplicationService.getUserDetail(userId);
    }

    @PostMapping
    public UserResponse create(@RequestBody @Valid CreateUserRequest request) {
        return userApplicationService.createUser(request);
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable("id") Long userId,
                               @RequestBody @Valid UpdateUserRequest request) {
        return userApplicationService.updateUser(userId, request);
    }
}
