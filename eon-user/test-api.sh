#!/bin/bash

echo "=== EON User Service API 测试 ==="

BASE_URL="http://localhost:3001"

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查服务是否启动
echo "🔍 检查服务健康状态..."
if curl -s "$BASE_URL/_health" | grep -q "UP"; then
    echo -e "${GREEN}✅ 服务正常运行${NC}"
else
    echo -e "${RED}❌ 服务未启动，请先运行 ./start.sh${NC}"
    exit 1
fi

echo ""
echo "🧪 开始API测试..."
echo ""

# 测试1：健康检查
echo "1️⃣ 测试健康检查接口"
echo -e "${YELLOW}GET $BASE_URL/_health${NC}"
curl -s "$BASE_URL/_health" | jq .
echo ""
echo ""

# 测试2：根路径
echo "2️⃣ 测试根路径"
echo -e "${YELLOW}GET $BASE_URL/${NC}"
curl -s "$BASE_URL/" | jq .
echo ""
echo ""

# 测试3：未认证访问受保护接口
echo "3️⃣ 测试未认证访问（应该返回401）"
echo -e "${YELLOW}GET $BASE_URL/auth/me/menus${NC}"
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/auth/me/menus")
http_code=$(echo "$response" | tail -n1)
if [ "$http_code" = "401" ]; then
    echo -e "${GREEN}✅ 正确返回401 Unauthorized${NC}"
else
    echo -e "${RED}❌ 期望401，实际返回$http_code${NC}"
fi
echo "$response" | head -n -1
echo ""
echo ""

# 测试4：登录
echo "4️⃣ 测试用户登录"
echo -e "${YELLOW}POST $BASE_URL/auth/login${NC}"
login_response=$(curl -s -X POST \
  "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "$login_response" | jq .

# 提取token
token=$(echo "$login_response" | jq -r '.token')
if [ "$token" != "null" ] && [ "$token" != "" ]; then
    echo -e "${GREEN}✅ 登录成功，获得token${NC}"
    echo "Token: $token"
else
    echo -e "${RED}❌ 登录失败${NC}"
    exit 1
fi
echo ""
echo ""

# 测试5：使用token访问受保护接口
echo "5️⃣ 测试认证后访问菜单接口"
echo -e "${YELLOW}GET $BASE_URL/auth/me/menus${NC}"
menu_response=$(curl -s \
  -H "Authorization: Bearer $token" \
  "$BASE_URL/auth/me/menus")
echo "$menu_response" | jq .

if echo "$menu_response" | jq -e 'type == "array"' > /dev/null; then
    echo -e "${GREEN}✅ 成功获取菜单数据${NC}"
else
    echo -e "${RED}❌ 菜单接口返回异常${NC}"
fi
echo ""
echo ""

# 测试6：错误的密码
echo "6️⃣ 测试错误密码（应该失败）"
echo -e "${YELLOW}POST $BASE_URL/auth/login (错误密码)${NC}"
error_response=$(curl -s -X POST \
  "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}')
echo "$error_response"

if echo "$error_response" | grep -q "Invalid credentials"; then
    echo -e "${GREEN}✅ 正确拒绝错误密码${NC}"
else
    echo -e "${RED}❌ 密码验证异常${NC}"
fi
echo ""
echo ""

# 测试7：无效token
echo "7️⃣ 测试无效token（应该返回401）"
echo -e "${YELLOW}GET $BASE_URL/auth/me/menus (无效token)${NC}"
invalid_token_response=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer invalid.token.here" \
  "$BASE_URL/auth/me/menus")
http_code=$(echo "$invalid_token_response" | tail -n1)
if [ "$http_code" = "401" ]; then
    echo -e "${GREEN}✅ 正确拒绝无效token${NC}"
else
    echo -e "${RED}❌ 期望401，实际返回$http_code${NC}"
fi
echo "$invalid_token_response" | head -n -1
echo ""

echo "🎉 API测试完成！"