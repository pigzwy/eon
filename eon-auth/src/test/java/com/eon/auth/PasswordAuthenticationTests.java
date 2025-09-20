package com.eon.auth;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OAuth2 Password授权类型集成测试
 * 
 * <p>此测试类验证OAuth2 Password授权模式的完整流程，包括：</p>
 * <ul>
 *   <li>正常的用户名密码认证流程</li>
 *   <li>客户端认证和授权验证</li>
 *   <li>错误场景的处理和响应</li>
 *   <li>令牌生成和刷新令牌支持</li>
 *   <li>授权范围(scope)的处理</li>
 * </ul>
 * 
 * <p><strong>测试环境设置：</strong></p>
 * <ul>
 *   <li>使用内存数据库H2进行数据存储</li>
 *   <li>预配置的测试用户：admin/123456, user1/password</li>
 *   <li>预配置的测试客户端：messaging-client/secret</li>
 * </ul>
 * 
 * @author EON Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PasswordAuthenticationTests {

    @Autowired
    private MockMvc mvc;

    /**
     * 测试成功的密码模式登录
     * 
     * <p>验证完整的password授权流程：</p>
     * <ol>
     *   <li>客户端通过HTTP Basic认证</li>
     *   <li>提供正确的用户名密码</li>
     *   <li>成功获取访问令牌和刷新令牌</li>
     * </ol>
     */
    @Test
    void testPasswordLoginSuccess() throws Exception {
        log.info("开始测试成功的密码模式登录...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .param(OAuth2ParameterNames.SCOPE, "openid profile read write")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.scope").value("openid profile read write"))
                .andExpect(jsonPath("$.expires_in").isNumber());
        
        log.info("密码模式登录测试成功完成");
    }

    /**
     * 测试使用第二个预配置用户的登录
     */
    @Test
    void testPasswordLoginWithUser1() throws Exception {
        log.info("开始测试user1用户的密码模式登录...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "user1")
                        .param(OAuth2ParameterNames.PASSWORD, "password")
                        .param(OAuth2ParameterNames.SCOPE, "read")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.scope").value("read"));
        
        log.info("user1密码模式登录测试成功完成");
    }

    /**
     * 测试错误的用户名密码
     * 
     * <p>验证当用户名或密码错误时，系统返回正确的OAuth2错误响应</p>
     */
    @Test
    void testPasswordLoginWithInvalidCredentials() throws Exception {
        log.info("开始测试错误的用户名密码场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "wrong-password")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"))
                .andExpect(jsonPath("$.error_description").value("用户名或密码不正确"));
        
        log.info("错误密码测试完成");
    }

    /**
     * 测试不存在的用户
     */
    @Test
    void testPasswordLoginWithNonExistentUser() throws Exception {
        log.info("开始测试不存在的用户场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "nonexistent")
                        .param(OAuth2ParameterNames.PASSWORD, "password")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
        
        log.info("不存在用户测试完成");
    }

    /**
     * 测试缺少必需参数的情况
     * 
     * <p>验证当缺少username或password参数时的错误处理</p>
     */
    @Test 
    void testPasswordLoginWithMissingUsername() throws Exception {
        log.info("开始测试缺少用户名参数场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"));
        
        log.info("缺少用户名测试完成");
    }

    /**
     * 测试缺少密码参数的情况
     */
    @Test
    void testPasswordLoginWithMissingPassword() throws Exception {
        log.info("开始测试缺少密码参数场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"));
        
        log.info("缺少密码测试完成");
    }

    /**
     * 测试客户端认证失败的情况
     * 
     * <p>验证当客户端凭证错误时的处理</p>
     */
    @Test
    void testPasswordLoginWithInvalidClientCredentials() throws Exception {
        log.info("开始测试客户端认证失败场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "wrong-secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
        
        log.info("客户端认证失败测试完成");
    }

    /**
     * 测试不支持password授权类型的客户端
     * 
     * <p>如果配置了不支持password模式的客户端，应该返回unauthorized_client错误</p>
     */
    @Test
    void testPasswordLoginWithUnsupportedGrantType() throws Exception {
        log.info("开始测试不支持的授权类型场景...");
        
        // 注意：这个测试需要一个不支持password授权类型的客户端
        // 如果所有客户端都支持password，这个测试会失败
        // 可以根据实际客户端配置调整
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("device-messaging-client", "");  // device client 不支持 password

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unauthorized_client"));
        
        log.info("不支持授权类型测试完成");
    }

    /**
     * 测试使用GET方法访问令牌端点
     * 
     * <p>OAuth2规范要求令牌端点只接受POST请求</p>
     */
    @Test
    void testPasswordLoginWithGetMethod() throws Exception {
        log.info("开始测试错误的HTTP方法场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(get("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
        
        log.info("错误HTTP方法测试完成");
    }

    /**
     * 测试无效的授权类型
     * 
     * <p>验证当grant_type不是"password"时的处理</p>
     */
    @Test
    void testInvalidGrantType() throws Exception {
        log.info("开始测试无效的授权类型场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "invalid_grant")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
        
        log.info("无效授权类型测试完成");
    }

    /**
     * 测试请求特定scope的情况
     * 
     * <p>验证scope参数的处理和授权范围限制</p>
     */
    @Test
    void testPasswordLoginWithSpecificScope() throws Exception {
        log.info("开始测试指定scope的场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .param(OAuth2ParameterNames.SCOPE, "read")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.scope").value("read"));
        
        log.info("指定scope测试完成");
    }

    /**
     * 测试请求多个scope的情况
     */
    @Test
    void testPasswordLoginWithMultipleScopes() throws Exception {
        log.info("开始测试多个scope的场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .param(OAuth2ParameterNames.SCOPE, "read write openid")
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.scope").value("openid read write"));  // scope可能被重新排序
        
        log.info("多个scope测试完成");
    }

    /**
     * 测试使用刷新令牌
     * 
     * <p>验证生成的刷新令牌可以用于获取新的访问令牌</p>
     */
    @Test
    void testRefreshToken() throws Exception {
        log.info("开始测试刷新令牌场景...");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("messaging-client", "secret");

        // 首先获取令牌
        String response = this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "password")
                        .param(OAuth2ParameterNames.USERNAME, "admin")
                        .param(OAuth2ParameterNames.PASSWORD, "123456")
                        .headers(headers))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 从响应中提取刷新令牌（这里需要JSON解析，简化示例）
        // 实际项目中应该使用JsonPath或ObjectMapper
        String refreshToken = extractRefreshToken(response);

        // 使用刷新令牌获取新的访问令牌
        this.mvc.perform(post("/oauth2/token")
                        .param(OAuth2ParameterNames.GRANT_TYPE, "refresh_token")
                        .param(OAuth2ParameterNames.REFRESH_TOKEN, refreshToken)
                        .headers(headers))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"));
        
        log.info("刷新令牌测试完成");
    }

    /**
     * 从JSON响应中提取刷新令牌
     * 
     * <p>这是一个简化的实现，实际项目中应该使用更健壮的JSON解析</p>
     */
    private String extractRefreshToken(String jsonResponse) {
        // 简化的JSON解析，实际应该使用JsonPath或ObjectMapper
        int startIndex = jsonResponse.indexOf("\"refresh_token\":\"") + 17;
        int endIndex = jsonResponse.indexOf("\"", startIndex);
        return jsonResponse.substring(startIndex, endIndex);
    }
}
