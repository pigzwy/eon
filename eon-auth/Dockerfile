# 基于多阶段构建打包 eon-auth 服务
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# 仅复制 Maven 构建所需的 POM，尽量复用依赖缓存
COPY pom.xml ./
COPY eon-auth/pom.xml eon-auth/
COPY eon-gateway/pom.xml eon-gateway/
COPY eon-user/pom.xml eon-user/
COPY eon-boot/pom.xml eon-boot/
COPY eon-common/pom.xml eon-common/
COPY eon-common/eon-common-bom/pom.xml eon-common/eon-common-bom/
COPY eon-common/eon-common-core/pom.xml eon-common/eon-common-core/
COPY eon-common/eon-common-datasource/pom.xml eon-common/eon-common-datasource/
COPY eon-common/eon-common-feign/pom.xml eon-common/eon-common-feign/
COPY eon-common/eon-common-log/pom.xml eon-common/eon-common-log/
COPY eon-common/eon-common-mybatis/pom.xml eon-common/eon-common-mybatis/
COPY eon-common/eon-common-oss/pom.xml eon-common/eon-common-oss/
COPY eon-common/eon-common-security/pom.xml eon-common/eon-common-security/
COPY eon-common/eon-common-swagger/pom.xml eon-common/eon-common-swagger/
COPY eon-common/eon-common-websocket/pom.xml eon-common/eon-common-websocket/
COPY eon-common/eon-common-xss/pom.xml eon-common/eon-common-xss/
COPY eon-common/eon-common-seata/pom.xml eon-common/eon-common-seata/
COPY eon-upms/pom.xml eon-upms/
COPY eon-upms/eon-upms-api/pom.xml eon-upms/eon-upms-api/
COPY eon-upms/eon-upms-biz/pom.xml eon-upms/eon-upms-biz/
COPY eon-visual/pom.xml eon-visual/
COPY eon-visual/eon-codegen/pom.xml eon-visual/eon-codegen/
COPY eon-visual/eon-monitor/pom.xml eon-visual/eon-monitor/
COPY eon-visual/eon-quartz/pom.xml eon-visual/eon-quartz/

# 预先拉取依赖，命中 BuildKit 缓存后后续构建加速
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl eon-auth -am dependency:go-offline -DskipTests

# 拷贝完整源码并执行实际构建
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl eon-auth -am package -DskipTests

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /opt/app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx512m"

# 将编译产物复制到运行镜像
COPY --from=builder /workspace/eon-auth/target/eon-auth-*.jar app.jar

EXPOSE 3000

# 通过 JAVA_OPTS 支持在 docker-compose 中覆盖 JVM 参数
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
