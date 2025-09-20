package com.eon.user.dto;

import com.eon.user.service.MenuService;

import java.util.List;

public class UserMeResponse {
    private UserResponse profile;
    private List<MenuService.MenuNode> menus;

    public UserResponse getProfile() {
        return profile;
    }

    public void setProfile(UserResponse profile) {
        this.profile = profile;
    }

    public List<MenuService.MenuNode> getMenus() {
        return menus;
    }

    public void setMenus(List<MenuService.MenuNode> menus) {
        this.menus = menus;
    }
}
