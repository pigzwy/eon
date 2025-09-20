package com.eon.user.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AssignPermissionsRequest {

    @NotEmpty(message = "权限 ID 列表不能为空")
    private List<Long> permissionIds;

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
