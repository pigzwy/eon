package com.eon.user.controller;

import com.eon.user.dto.AssignPermissionsRequest;
import com.eon.user.dto.CreateRoleRequest;
import com.eon.user.dto.RoleResponse;
import com.eon.user.service.UserApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final UserApplicationService userApplicationService;

    public RoleController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @GetMapping
    public List<RoleResponse> list(@RequestParam(value = "tenantId", required = false) Long tenantId) {
        return userApplicationService.listRoles(tenantId);
    }

    @PostMapping
    public RoleResponse create(@RequestBody @Valid CreateRoleRequest request) {
        return userApplicationService.createRole(request);
    }

    @PostMapping("/{id}/permissions")
    public RoleResponse assignPermissions(@PathVariable("id") Long roleId,
                                          @RequestBody @Valid AssignPermissionsRequest request) {
        return userApplicationService.assignPermissions(roleId, request);
    }
}
