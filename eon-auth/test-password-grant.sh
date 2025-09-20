#!/bin/bash

# OAuth2 Password授权类型测试脚本
# 
# 此脚本演示了如何使用curl命令测试OAuth2 Password授权的各种场景
# 
# 使用方法：
#   chmod +x test-password-grant.sh
#   ./test-password-grant.sh
#
# 注意：确保授权服务器在 http://localhost:3000 运行

set -e

# 颜色输出定义
RED='\033[0;31m'
GREEN='\033[0;32m'  
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 服务器配置
SERVER_URL="http://localhost:3000"
TOKEN_ENDPOINT="${SERVER_URL}/oauth2/token"

# 客户端凭证（Base64编码的 messaging-client:secret）
CLIENT_AUTH="Basic bWVzc2FnaW5nLWNsaWVudDpzZWNyZXQ="

# 打印分隔线
print_separator() {
    echo -e "${BLUE}=================================================${NC}"
}

# 打印测试标题
print_test_title() {
    echo -e "\n${YELLOW}🧪 测试: $1${NC}"
    print_separator
}

# 打印成功消息
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# 打印错误消息  
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 打印信息消息
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# 检查服务器是否运行
check_server() {
    print_info "检查授权服务器是否运行..."
    if curl -s -f "${SERVER_URL}/.well-known/oauth-authorization-server" > /dev/null; then
        print_success "授权服务器运行正常"
    else
        print_error "无法连接到授权服务器 ${SERVER_URL}"
        print_info "请确保应用已启动：mvn spring-boot:run"
        exit 1
    fi
}

# 执行HTTP请求并处理响应
make_request() {
    local description="$1"
    local curl_args="$2"
    local expected_status="$3"
    
    echo -e "\n📡 请求: ${description}"
    echo "命令: curl ${curl_args}"
    echo ""
    
    # 执行请求并捕获状态码和响应体
    local response=$(eval "curl -s -w \"HTTPSTATUS:%{http_code}\" ${curl_args}")
    local status_code=$(echo "$response" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    local body=$(echo "$response" | sed 's/HTTPSTATUS:[0-9]*$//')
    
    echo "状态码: $status_code"
    echo "响应体:"
    echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
    
    # 验证状态码
    if [[ "$status_code" == "$expected_status" ]]; then
        print_success "测试通过 (状态码: $status_code)"
        
        # 如果是成功的令牌请求，提取access_token用于后续测试
        if [[ "$status_code" == "200" && "$body" == *"access_token"* ]]; then
            ACCESS_TOKEN=$(echo "$body" | python3 -c "import sys, json; print(json.load(sys.stdin)['access_token'])" 2>/dev/null || echo "")
            REFRESH_TOKEN=$(echo "$body" | python3 -c "import sys, json; print(json.load(sys.stdin).get('refresh_token', ''))" 2>/dev/null || echo "")
            if [[ -n "$ACCESS_TOKEN" ]]; then
                print_info "已提取访问令牌供后续测试使用"
            fi
        fi
    else
        print_error "测试失败 (预期: $expected_status, 实际: $status_code)"
    fi
    
    print_separator
}

# 测试用例定义

# 1. 成功的密码模式登录
test_successful_login() {
    print_test_title "成功的密码模式登录"
    make_request "使用正确的用户凭证获取令牌" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: ${CLIENT_AUTH}\" \
         -d \"grant_type=password&username=admin&password=123456&scope=read write openid profile\"" \
        "200"
}

# 2. 错误的用户密码
test_invalid_credentials() {
    print_test_title "错误的用户密码"
    make_request "使用错误的密码" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: ${CLIENT_AUTH}\" \
         -d \"grant_type=password&username=admin&password=wrongpassword\"" \
        "400"
}

# 3. 不存在的用户
test_nonexistent_user() {
    print_test_title "不存在的用户"
    make_request "使用不存在的用户名" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: ${CLIENT_AUTH}\" \
         -d \"grant_type=password&username=nonexistent&password=password\"" \
        "400"
}

# 4. 缺少必需参数
test_missing_username() {
    print_test_title "缺少用户名参数"
    make_request "请求中缺少username参数" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: ${CLIENT_AUTH}\" \
         -d \"grant_type=password&password=123456\"" \
        "400"
}

test_missing_password() {
    print_test_title "缺少密码参数"
    make_request "请求中缺少password参数" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: ${CLIENT_AUTH}\" \
         -d \"grant_type=password&username=admin\"" \
        "400"
}

# 5. 客户端认证失败
test_invalid_client() {
    print_test_title "客户端认证失败"
    make_request "使用错误的客户端凭证" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: Basic d3JvbmdjbGllbnQ6d3JvbmdzZWNyZXQ=\" \
         -d \"grant_type=password&username=admin&password=123456\"" \
        "401"
}

# 6. 不支持的授权类型
test_unsupported_grant_type() {
    print_test_title "不支持的授权类型"
    make_request "使用无效的grant_type" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: ${CLIENT_AUTH}\" \
         -d \"grant_type=invalid_grant&username=admin&password=123456\"" \
        "400"
}

# 7. 错误的HTTP方法
test_wrong_http_method() {
    print_test_title "错误的HTTP方法"
    make_request "使用GET方法访问令牌端点" \
        "-X GET \"${TOKEN_ENDPOINT}?grant_type=password&username=admin&password=123456\" \
         -H \"Authorization: ${CLIENT_AUTH}\"" \
        "405"
}

# 8. 特定scope请求
test_specific_scope() {
    print_test_title "请求特定scope"
    make_request "只请求read scope" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: ${CLIENT_AUTH}\" \
         -d \"grant_type=password&username=admin&password=123456&scope=read\"" \
        "200"
}

# 9. 刷新令牌测试
test_refresh_token() {
    print_test_title "使用刷新令牌"
    
    if [[ -n "$REFRESH_TOKEN" ]]; then
        make_request "使用刷新令牌获取新的访问令牌" \
            "-X POST \"${TOKEN_ENDPOINT}\" \
             -H \"Content-Type: application/x-www-form-urlencoded\" \
             -H \"Authorization: ${CLIENT_AUTH}\" \
             -d \"grant_type=refresh_token&refresh_token=${REFRESH_TOKEN}\"" \
            "200"
    else
        print_error "没有可用的刷新令牌，跳过此测试"
        print_info "请确保先运行成功的登录测试以获取刷新令牌"
    fi
}

# 10. 使用第二个测试用户
test_second_user() {
    print_test_title "第二个测试用户登录"
    make_request "使用user1用户登录" \
        "-X POST \"${TOKEN_ENDPOINT}\" \
         -H \"Content-Type: application/x-www-form-urlencoded\" \
         -H \"Authorization: ${CLIENT_AUTH}\" \
         -d \"grant_type=password&username=user1&password=password&scope=read\"" \
        "200"
}

# 11. 服务器元数据查询
test_server_metadata() {
    print_test_title "查询服务器元数据"
    make_request "获取授权服务器配置信息" \
        "-X GET \"${SERVER_URL}/.well-known/oauth-authorization-server\" \
         -H \"Accept: application/json\"" \
        "200"
}

# 主测试流程
main() {
    echo -e "${GREEN}"
    echo "🚀 OAuth2 Password授权类型测试套件"
    echo "======================================"
    echo -e "${NC}"
    
    print_info "开始执行测试..."
    
    # 检查服务器状态
    check_server
    
    # 执行所有测试用例
    test_successful_login        # 必须先执行，为后续测试提供令牌
    test_invalid_credentials
    test_nonexistent_user  
    test_missing_username
    test_missing_password
    test_invalid_client
    test_unsupported_grant_type
    test_wrong_http_method
    test_specific_scope
    test_second_user
    test_refresh_token           # 依赖于第一个成功的登录测试
    test_server_metadata
    
    print_separator
    echo -e "\n${GREEN}🎉 所有测试已完成！${NC}"
    echo -e "${BLUE}查看上述结果了解各种场景的行为表现。${NC}\n"
}

# 帮助信息
show_help() {
    echo "OAuth2 Password授权类型测试脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -s, --server   指定服务器URL (默认: http://localhost:3000)"
    echo ""
    echo "示例:"
    echo "  $0                    # 使用默认服务器地址运行所有测试"
    echo "  $0 -s http://localhost:8080  # 使用自定义服务器地址"
    echo ""
    echo "先决条件:"
    echo "  1. 授权服务器必须正在运行"
    echo "  2. 系统中必须安装 python3 (用于JSON格式化)"
    echo "  3. 系统中必须安装 curl"
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -s|--server)
            SERVER_URL="$2"
            TOKEN_ENDPOINT="${SERVER_URL}/oauth2/token"
            shift 2
            ;;
        *)
            echo "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
done

# 检查依赖
check_dependencies() {
    local missing_deps=()
    
    if ! command -v curl &> /dev/null; then
        missing_deps+=("curl")
    fi
    
    if ! command -v python3 &> /dev/null; then
        missing_deps+=("python3")
    fi
    
    if [[ ${#missing_deps[@]} -gt 0 ]]; then
        print_error "缺少必要依赖: ${missing_deps[*]}"
        print_info "请安装缺少的依赖后重试"
        exit 1
    fi
}

# 执行主流程
check_dependencies
main