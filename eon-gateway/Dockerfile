# 多阶段构建 eon-gateway 服务镜像
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

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

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl eon-gateway -am dependency:go-offline -DskipTests

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl eon-gateway -am package -DskipTests

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /opt/app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx512m"

COPY --from=builder /workspace/eon-gateway/target/eon-gateway-*.jar app.jar

EXPOSE 3100
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
