#!/bin/bash

echo "=== EON User Service 启动脚本 ==="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请启动Docker"
    exit 1
fi

# 启动基础设施
echo "🚀 启动基础设施 (MySQL + Redis + Nacos)..."
docker compose up -d

# 等待MySQL和Nacos启动
echo "⏳ 等待MySQL和Nacos启动..."
sleep 30

# 检查MySQL是否就绪
echo "🔍 检查MySQL连接..."
until docker exec eon-mysql mysql -uroot -proot -e "SELECT 1" >/dev/null 2>&1; do
    echo "等待MySQL启动..."
    sleep 5
done
echo "✅ MySQL 已就绪"

# 检查Nacos是否就绪
echo "🔍 检查Nacos连接..."
until curl -s http://localhost:8848/nacos/actuator/health | grep -q '"status":"UP"'; do
    echo "等待Nacos启动..."
    sleep 5
done
echo "✅ Nacos 已就绪"

# 编译项目
echo "🔨 编译项目..."
cd ..
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi
echo "✅ 编译完成"

cd eon-user

# 启动应用
echo "🚀 启动EON User Service..."
java -jar target/eon-user-*.jar

echo "🎉 EON User Service 启动完成！"
echo ""
echo "🔗 服务地址:"
echo "  - EON User API: http://localhost:3001"
echo "  - 健康检查: http://localhost:3001/_health"
echo "  - MySQL: localhost:3306 (root/root)"
echo "  - Nacos: http://localhost:8848/nacos (nacos/nacos)"
echo ""
echo "📋 测试命令:"
echo '  curl http://localhost:3001/actuator/health'
echo '  curl -H "X-User-Id:1" http://localhost:3001/users/me'
