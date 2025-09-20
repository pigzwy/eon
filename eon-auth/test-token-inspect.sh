#!/bin/bash

# OAuth2 访问令牌巡检脚本
#
# 用途：
#   1. 快速校验授权服务器是否可达
#   2. 调用 /userinfo 端点读取令牌中的用户名、角色、权限
#   3. 调用 /oauth2/introspect 端点验证令牌是否仍然有效
#   4. 本地解码 JWT，直观查看声明、过期时间等关键信息
#
# 使用方式：
#   ./test-token-inspect.sh "<access_token>"
#   ACCESS_TOKEN="..." ./test-token-inspect.sh
#
# 可配环境变量：
#   SERVER_URL     授权服务器地址，默认 http://localhost:3000
#   CLIENT_ID      用于内省的客户端ID，默认 eon
#   CLIENT_SECRET  用于内省的客户端密钥，默认 eon

set -euo pipefail

SERVER_URL=${SERVER_URL:-http://localhost:3000}
CLIENT_ID=${CLIENT_ID:-eon}
CLIENT_SECRET=${CLIENT_SECRET:-eon}
ACCESS_TOKEN=${1:-${ACCESS_TOKEN:-}}

if [[ -z "${ACCESS_TOKEN}" ]]; then
  echo "缺少访问令牌，请通过参数或 ACCESS_TOKEN 环境变量传入" >&2
  echo "示例：ACCESS_TOKEN=ey... ./test-token-inspect.sh" >&2
  exit 1
fi

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "缺少依赖命令: $1，请先安装" >&2
    exit 1
  fi
}

require_cmd curl
require_cmd python3

print_section() {
  printf '\n%s\n' "=================================================="
  printf "🔍 %s\n" "$1"
  printf '%s\n' "=================================================="
}

pretty_json() {
  if command -v jq >/dev/null 2>&1; then
    jq . 2>/dev/null || cat
  else
    python3 - <<'PY'
import json, sys
try:
    data = json.load(sys.stdin)
    print(json.dumps(data, ensure_ascii=False, indent=2))
except Exception:
    sys.stdout.write(sys.stdin.read())
PY
  fi
}

check_server() {
  print_section "检测授权服务器可用性"
  if curl -sSf "${SERVER_URL}/.well-known/oauth-authorization-server" >/dev/null; then
    echo "✅ 授权服务器可用 (${SERVER_URL})"
  else
    echo "❌ 无法访问授权服务器，请确认服务已启动" >&2
    exit 1
  fi
}

decode_jwt() {
  print_section "本地解码 JWT"
  python3 - "$ACCESS_TOKEN" <<'PY'
import base64
import datetime as dt
import json
import sys

token = sys.argv[1]
parts = token.split('.')
if len(parts) < 2:
    print("❌ 令牌格式异常，无法拆分 Header/Payload")
    sys.exit(1)

header_b64, payload_b64 = parts[0], parts[1]

def decode(segment):
    padding = '=' * (-len(segment) % 4)
    return base64.urlsafe_b64decode(segment + padding)

header = json.loads(decode(header_b64))
payload = json.loads(decode(payload_b64))

print("Header:")
print(json.dumps(header, ensure_ascii=False, indent=2))
print("\nPayload:")
print(json.dumps(payload, ensure_ascii=False, indent=2))

now = dt.datetime.now(dt.timezone.utc)
if "exp" in payload:
    exp = dt.datetime.fromtimestamp(payload["exp"], tz=dt.timezone.utc)
    delta = exp - now
    expires_in = int(delta.total_seconds())
    print(f"\n过期时间(UTC)：{exp.isoformat()}  距离过期：{expires_in} 秒")
if "iat" in payload:
    issued = dt.datetime.fromtimestamp(payload["iat"], tz=dt.timezone.utc)
    print(f"签发时间(UTC)：{issued.isoformat()}")
if "sub" in payload:
    print(f"主体(sub)：{payload['sub']}")
roles = payload.get("roles")
if roles:
    print("角色(roles)：", roles)
permissions = payload.get("permissions")
if permissions:
    print("权限(permissions)：", permissions)
auths = payload.get("authorities")
if auths:
    print("合并权限(authorities)：", auths)
PY
}

call_userinfo() {
  print_section "调用 /userinfo 端点"
  response=$(curl -s -o - -w $'HTTPSTATUS:%{http_code}\nREDIRECT:%{redirect_url}' \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H "Accept: application/json" \
    "${SERVER_URL}/userinfo")
  status=$(echo "$response" | awk -F'HTTPSTATUS:' 'NF>1 {print $2}' | awk -F'\n' 'NR==1{print $1}')
  redirect=$(echo "$response" | awk -F'REDIRECT:' 'NF>1 {print $2}' | tail -n1)
  body=$(echo "$response" | sed -E 's/HTTPSTATUS:[0-9]+\nREDIRECT:.*$//')
  echo "HTTP 状态码: ${status}"
  if [[ "$status" == "200" ]]; then
    echo "$body" | pretty_json
  else
    echo "$body"
    if [[ "$status" == "302" && $redirect == */login* ]]; then
      echo "⚠️ 被重定向到登录页，通常表示授权服务器未启用 OIDC /userinfo 支持或请求未携带 Bearer 令牌"
    else
      echo "⚠️ /userinfo 返回非成功状态，请确认令牌 scope 包含 openid/profile 并确保令牌仍然有效"
    fi
  fi
}

call_introspect() {
  print_section "调用 /oauth2/introspect 端点"
  response=$(curl -s -w '\n%{http_code}' \
    -u "${CLIENT_ID}:${CLIENT_SECRET}" \
    -d "token=${ACCESS_TOKEN}" \
    -d "token_type_hint=access_token" \
    "${SERVER_URL}/oauth2/introspect")
  status=${response##*$'\n'}
  body=${response%$'\n'*}
  echo "HTTP 状态码: ${status}"
  echo "$body" | pretty_json

  if [[ "$status" != "200" ]]; then
    echo "⚠️ 内省端点返回异常状态，请确认客户端凭证正确"
    return
  fi

  active=$(echo "$body" | python3 - <<'PY'
import json, sys
try:
    data = json.load(sys.stdin)
    print(str(data.get("active", False)).lower())
except Exception:
    print("false")
PY
  )

  if [[ "$active" != "true" ]]; then
    echo "⚠️ introspect 报告 active=false，常见原因："
    echo "   1. 传入的并非访问令牌（可能是刷新令牌或ID Token）"
    echo "   2. 令牌已过期或被撤销——请参考上方 JWT 解码结果中的 exp"
    echo "   3. 数据库 oauth2_authorization 表为空，授权记录未落库"
    echo "   4. CLIENT_ID/CLIENT_SECRET 无权限调用 introspect"
  fi
}

check_server
decode_jwt
call_userinfo
call_introspect

print_section "完成"
echo "✅ 令牌巡检流程结束，可根据上方输出判断令牌可用性"
