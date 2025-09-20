package com.eon.user.util;

public class PathPatternCompiler {

    /**
     * 将路径模板编译为正则表达式
     * /users/:id      -> ^/users/[^/]+$
     * /orders/**      -> ^/orders/.*$
     * /files/:path**  -> ^/files/.+$
     */
    public static String toRegex(String template) {
        String esc = template
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\?", "\\\\?")
                .replaceAll("\\+", "\\\\+");
        
        // :param -> [^/]+
        esc = esc.replaceAll("(:[a-zA-Z_][a-zA-Z0-9_]*)", "[^/]+");
        
        // ** -> .*
        esc = esc.replace("/**", "/.*");
        
        // 剩余 * 不推荐在模板中使用，若有可按需处理
        if (!esc.startsWith("^")) esc = "^" + esc;
        if (!esc.endsWith("$")) esc = esc + "$";
        return esc;
    }
}