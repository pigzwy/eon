package com.eon.gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * 简易路径匹配工具（Ant 风格）。
 */
public final class PathMatcherUtils {
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private PathMatcherUtils() {}

    public static boolean anyMatch(String path, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) return false;
        for (String p : patterns) {
            if (MATCHER.match(p, path)) return true;
        }
        return false;
    }
}

