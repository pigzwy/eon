package com.eon.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 权限鉴权过滤器：基于用户角色进行API访问控制，网关层的第二道安全防线
 * 
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>路由权限检查</b>：从路由元数据中获取访问所需的角色要求</li>
 *   <li><b>用户权限匹配</b>：从请求上下文中获取用户的角色信息并进行匹配</li>
 *   <li><b>访问拦截</b>：拦截无权限访问的请求，保护后端服务安全</li>
 *   <li><b>错误响应</b>：返回统一的权限错误响应，避免系统信息泄露</li>
 * </ul>
 * 
 * <p>执行时机：</p>
 * <ul>
 *   <li>执行顺序：-5（在JwtAuthenticationFilter之后执行）</li>
 *   <li>适用范围：所有需要权限控制的API请求</li>
 *   <li>拦截位置：网关层，请求到达后端服务之前</li>
 * </ul>
 * 
 * <p>技术特点：</p>
 * <ul>
 *   <li><b>配置驱动</b>：通过路由元数据灵活配置权限要求，无需硬编码</li>
 *   <li><b>高性能</b>：基于内存中的字符串比对，鉴权操作在微秒级别完成</li>
 *   <li><b>可扩展</b>：支持多种权限配置格式（字符串、数组、列表）</li>
 *   <li><b>无状态</b>：不依赖会话状态，基于JWT Token的权限信息</li>
 * </ul>
 * 
 * <p>权限配置示例：</p>
 * <pre>
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *         - id: admin-api
 *           uri: lb://admin-service
 *           predicates:
 *             - Path=/api/admin/**
 *           metadata:
 *             requiredRoles: ADMIN  # 需要ADMIN角色
 *         - id: management-api
 *           uri: lb://management-service  
 *           predicates:
 *             - Path=/api/management/**
 *           metadata:
 *             requiredRoles: [ADMIN, MANAGER]  # 需要ADMIN或MANAGER角色
 * </pre>
 * 
 * @author EON Framework Team
 * @version 1.0.0
 * @since 2025-09-17
 */
@Component
public class AuthorizationFilter implements GlobalFilter, Ordered {

    /**
     * 权限鉴权核心方法：基于角色的权限匹配和访问控制
     * 
     * <p>详细执行流程：</p>
     * <ol>
     *   <li><b>路由信息获取</b>：从请求上下文中提取当前访问的路由信息</li>
     *   <li><b>权限要求解析</b>：从路由元数据中获取访问该路由所需的角色要求</li>
     *   <li><b>用户权限获取</b>：从请求上下文中获取用户的角色信息（由JwtAuthenticationFilter设置）</li>
     *   <li><b>权限匹配检查</b>：检查用户是否拥有任意一个所需角色</li>
     *   <li><b>访问决策</b>：根据匹配结果决定放行请求或返回权限错误</li>
     * </ol>
     * 
     * <p>权限匹配策略：</p>
     * <ul>
     *   <li><b>OR逻辑</b>：用户拥有任意一个所需角色即可访问</li>
     *   <li><b>大小写敏感</b>：角色名称区分大小写（如"ADMIN" ≠ "admin"）</li>
     *   <li><b>空配置</b>：路由未配置权限要求时，默认公开访问</li>
     *   <li><b>无角色</b>：用户无角色信息时，默认拒绝访问（安全优先）</li>
     * </ul>
     * 
     * <p>性能优化：</p>
     * <ul>
     *   <li><b>短路逻辑</b>：找到匹配角色后立即返回，避免不必要的遍历</li>
     *   <li><b>内存操作</b>：所有操作都在内存中完成，无数据库查询</li>
     *   <li><b>高效比对</b>：使用String.equals()进行快速字符串比对</li>
     * </ul>
     * 
     * @param exchange HTTP请求交换器，包含请求信息和上下文数据
     * @param chain 过滤器链，用于调用后续过滤器或目标服务
     * @return 异步处理结果，成功时继续过滤器链，失败时返回错误响应
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 第一步：获取当前请求的路由信息
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            return chain.filter(exchange);  // 没有路由信息，直接放行（异常情况）
        }
        
        // 第二步：获取路由的元数据配置
        Map<String, Object> metadata = route.getMetadata();
        if (metadata == null) {
            return chain.filter(exchange);  // 没有元数据，直接放行（公开路由）
        }
        
        // 第三步：从元数据中获取访问该路由所需的角色要求
        Object required = metadata.get("requiredRoles");
        List<String> requiredRoles = toStringList(required);
        if (requiredRoles.isEmpty()) {
            return chain.filter(exchange);  // 没有角色要求，公开访问
        }
        
        // 第四步：从请求上下文中获取用户的角色信息
        // 由JwtAuthenticationFilter在JWT验证成功后设置
        List<String> userRoles = exchange.getAttributeOrDefault(JwtAuthenticationFilter.ATTR_ROLES, List.of());
        if (CollectionUtils.isEmpty(userRoles)) {
            return forbidden(exchange, "缺少访问所需角色");  // 用户没有角色信息，拒绝访问
        }
        
        // 第五步：检查用户是否拥有任意一个所需角色
        for (String role : requiredRoles) {
            if (userRoles.contains(role)) {
                return chain.filter(exchange);  // 权限校验通过，放行请求
            }
        }
        
        // 第六步：权限校验失败，返回禁止访问响应
        return forbidden(exchange, "没有访问该资源的权限");
    }

    /**
     * 将对象转换为字符串列表：支持多种权限配置格式的灵活转换
     * 
     * <p>支持的数据格式：</p>
     * <ul>
     *   <li><b>字符串</b>："ADMIN" → ["ADMIN"]</li>
     *   <li><b>列表/数组</b>：["ADMIN", "MANAGER"] → ["ADMIN", "MANAGER"]</li>
     *   <li><b>单个对象</b>：ADMIN → ["ADMIN"]</li>
     *   <li><b>null值</b>：null → []（空列表）</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <pre>
     * # YAML配置示例1：字符串格式
     * metadata:
     *   requiredRoles: "ADMIN"
     * 
     * # YAML配置示例2：数组格式
     * metadata:
     *   requiredRoles:
     *     - "ADMIN"
     *     - "MANAGER"
     * 
     * # YAML配置示例3：单元素列表格式
     * metadata:
     *   requiredRoles: ["ADMIN"]
     * </pre>
     * 
     * <p>转换规则：</p>
     * <ul>
     *   <li><b>null处理</b>：返回空列表，表示无权限要求</li>
     *   <li><b>空值过滤</b>：过滤掉列表中的null元素，确保数据质量</li>
     *   <li><b>类型转换</b>：所有元素都转换为String类型，确保类型一致性</li>
     *   <li><b>安全性</b>：防御性编程，处理各种异常输入情况</li>
     * </ul>
     * 
     * @param raw 原始对象，可以是String、List、数组或其他类型
     * @return 字符串列表，包含所有有效的角色要求，如果输入为null或空则返回空列表
     */
    private List<String> toStringList(Object raw) {
        if (raw == null) {
            return List.of();  // 输入为null，返回空列表
        }
        if (raw instanceof String str) {
            return List.of(str);  // 字符串类型，转换为单元素列表
        }
        if (raw instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    result.add(String.valueOf(item));  // 过滤null值并转换为字符串
                }
            }
            return result;  // 返回处理后的列表
        }
        return List.of(String.valueOf(raw));  // 其他类型转换为单元素列表
    }

    /**
     * 返回禁止访问响应：构建统一的403权限错误响应
     * 
     * <p>响应格式：</p>
     * <pre>
     * HTTP/1.1 403 Forbidden
     * Content-Type: application/json;charset=UTF-8
     * Content-Length: 75
     * 
     * {
     *   "code": "FORBIDDEN",
     *   "message": "没有访问该资源的权限"
     * }
     * </pre>
     * 
     * <p>安全特性：</p>
     * <ul>
     *   <li><b>统一格式</b>：所有权限错误都使用统一的响应格式，便于客户端处理</li>
     *   <li><b>信息隐藏</b>：不暴露系统内部信息，只返回用户友好的错误消息</li>
     *   <li><b>字符编码</b>：使用UTF-8编码，支持中文错误信息</li>
     *   <li><b>内容类型</b>：明确指定JSON格式，便于客户端解析</li>
     * </ul>
     * 
     * <p>性能考虑：</p>
     * <ul>
     *   <li><b>零拷贝</b>：使用Mono.just包装字节数组，避免不必要的内存拷贝</li>
     *   <li><b>响应缓冲</b>：使用bufferFactory包装响应体，支持流式处理</li>
     *   <li><b>快速响应</b>：直接构建错误响应，无复杂计算逻辑</li>
     * </ul>
     * 
     * @param exchange HTTP请求交换器，用于设置响应状态和头信息
     * @param message 错误消息，描述权限失败的具体原因
     * @return 异步处理结果，包含完整错误响应的Mono对象
     */
    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        // 设置HTTP状态码为403 Forbidden
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        
        // 设置响应头为JSON格式
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // 构建JSON响应体，包含错误码和错误消息
        byte[] body = ("{\"code\":\"FORBIDDEN\",\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        
        // 返回异步响应，使用响应缓冲工厂包装字节数组
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    /**
     * 获取过滤器执行顺序：控制过滤器在过滤器链中的执行位置
     * 
     * <p>执行顺序设计：</p>
     * <ul>
     *   <li><b>顺序值</b>：-5（在JwtAuthenticationFilter之后执行）</li>
     *   <li><b>执行时机</b>：在JWT认证完成后，但在业务处理之前</li>
     *   <li><b>依赖关系</b>：依赖JwtAuthenticationFilter设置的用户角色信息</li>
     * </ul>
     * 
     * <p>过滤器链执行顺序：</p>
     * <pre>
     * 1. JwtAuthenticationFilter (-10)  - JWT认证和用户信息提取
     * 2. AuthorizationFilter (-5)        - 权限鉴权和访问控制
     * 3. 其他业务过滤器                   - 业务逻辑处理
     * 4. 目标服务调用                    - 转发到后端服务
     * </pre>
     * 
     * <p>设计考虑：</p>
     * <ul>
     *   <li><b>数字越小优先级越高</b>：负数表示高优先级</li>
     *   <li><b>依赖前置</b>：确保JWT认证先于权限鉴权执行</li>
     *   <li><b>性能优化</b>：在网关层拦截无效请求，避免转发到后端</li>
     * </ul>
     * 
     * @return 过滤器执行顺序，数值越小优先级越高
     */
    @Override
    public int getOrder() {
        return -5;  // 在JwtAuthenticationFilter(-10)之后执行
    }
}
