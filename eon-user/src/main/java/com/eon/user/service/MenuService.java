package com.eon.user.service;

import com.eon.user.entity.Menu;
import com.eon.user.policy.CompiledPolicy;
import com.eon.user.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepo;

    public MenuService(MenuRepository menuRepo) {
        this.menuRepo = menuRepo;
    }

    public static class MenuNode {
        private Long id;
        private Long parentId;
        private String title;
        private String routePath;
        private String icon;
        private Integer sortWeight;
        private String permissionKey;
        private List<MenuNode> children = new ArrayList<>();

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getRoutePath() { return routePath; }
        public void setRoutePath(String routePath) { this.routePath = routePath; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public Integer getSortWeight() { return sortWeight; }
        public void setSortWeight(Integer sortWeight) { this.sortWeight = sortWeight; }

        public String getPermissionKey() { return permissionKey; }
        public void setPermissionKey(String permissionKey) { this.permissionKey = permissionKey; }

        public List<MenuNode> getChildren() { return children; }
        public void setChildren(List<MenuNode> children) { this.children = children; }
    }

    public List<MenuNode> userMenus(Long userId, Long tenantId, CompiledPolicy policy) {
        List<Menu> all = menuRepo.findByTenantIdOrTenantIdIsNull(tenantId);
        // 过滤：visible && 有 view=ALLOW
        List<Menu> allowed = all.stream()
                .filter(m -> (m.getVisible() == null || m.getVisible())
                        && "ALLOW".equals(policy.getMenuEffects().getOrDefault(m.getPermissionKey(), "DENY")))
                .sorted(Comparator.comparing(Menu::getSortWeight, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        Map<Long, MenuNode> map = new HashMap<>();
        for (Menu m : allowed) {
            MenuNode n = new MenuNode();
            n.setId(m.getId());
            n.setParentId(m.getParentId());
            n.setTitle(m.getTitle());
            n.setRoutePath(m.getRoutePath());
            n.setIcon(m.getIcon());
            n.setSortWeight(m.getSortWeight());
            n.setPermissionKey(m.getPermissionKey());
            map.put(m.getId(), n);
        }
        
        // 组装树
        List<MenuNode> roots = new ArrayList<>();
        for (MenuNode n : map.values()) {
            if (n.getParentId() == null || !map.containsKey(n.getParentId())) {
                roots.add(n);
            } else {
                map.get(n.getParentId()).getChildren().add(n);
            }
        }
        
        // 子项排序
        roots.forEach(this::sortRecursively);
        roots.sort(Comparator.comparing(MenuNode::getSortWeight, Comparator.nullsLast(Integer::compareTo)));
        return roots;
    }

    private void sortRecursively(MenuNode node) {
        node.getChildren().sort(Comparator.comparing(MenuNode::getSortWeight, Comparator.nullsLast(Integer::compareTo)));
        node.getChildren().forEach(this::sortRecursively);
    }
}