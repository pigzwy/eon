[Root Directory](../../CLAUDE.md) > **eon-visual**

# EON 可视化工具模块

## 变更日志 (Changelog)

### v1.0.0 (2025-09-16)
- **模块初始化**: 建立可视化工具集文档
- **核心功能**: 提供监控、代码生成、定时任务等工具
- **模块完善**: 集成常用的可视化运维工具

## 模块职责

eon-visual是EON框架的可视化工具模块，提供一系列运维和开发支持工具，包括服务监控、代码生成、定时任务管理等功能。该模块旨在提高开发和运维效率，提供直观的操作界面。

## 模块结构

```
eon-visual/
├── eon-monitor/             # 服务监控
│   ├── src/main/java/com/eon/visual/monitor/
│   │   ├── EonMonitorApplication.java
│   │   ├── controller/     # 监控控制器
│   │   ├── service/        # 监控服务
│   │   └── config/         # 监控配置
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── eon-codegen/            # 代码生成器
│   ├── src/main/java/com/eon/visual/codegen/
│   │   ├── EonCodegenApplication.java
│   │   ├── controller/     # 代码生成控制器
│   │   ├── service/        # 代码生成服务
│   │   ├── generator/      # 代码生成器
│   │   └── template/       # 代码模板
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── eon-quartz/             # 定时任务管理
│   ├── src/main/java/com/eon/visual/quartz/
│   │   ├── EonQuartzApplication.java
│   │   ├── controller/     # 任务控制器
│   │   ├── service/        # 任务服务
│   │   ├── job/           # 任务定义
│   │   └── config/        # 任务配置
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
└── pom.xml
```

## 子模块说明

### eon-monitor - 服务监控
**端口**: 5001
**功能**: 提供服务监控、性能指标展示、告警管理等功能

**主要特性**:
- 服务健康状态监控
- 系统资源监控（CPU、内存、磁盘、网络）
- 应用性能监控（APM）
- 日志聚合和查询
- 告警规则配置
- 可视化仪表板

### eon-codegen - 代码生成器
**端口**: 5002
**功能**: 基于数据库表结构生成CRUD代码

**主要特性**:
- 数据库表结构读取
- 代码模板配置
- 实体类生成
- Mapper接口生成
- Service接口和实现生成
- Controller生成
- 前端页面生成

### eon-quartz - 定时任务管理
**端口**: 5007
**功能**: 定时任务管理和调度

**主要特性**:
- 任务配置管理
- 任务执行监控
- 任务日志记录
- 任务依赖管理
- 任务暂停/恢复
- 任务执行历史

## 入口和启动

### 监控服务启动
```java
// EonMonitorApplication.java
@SpringBootApplication
public class EonMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(EonMonitorApplication.class, args);
    }
}
```

### 代码生成器启动
```java
// EonCodegenApplication.java
@SpringBootApplication
public class EonCodegenApplication {
    public static void main(String[] args) {
        SpringApplication.run(EonCodegenApplication.class, args);
    }
}
```

### 定时任务管理启动
```java
// EonQuartzApplication.java
@SpringBootApplication
public class EonQuartzApplication {
    public static void main(String[] args) {
        SpringApplication.run(EonQuartzApplication.class, args);
    }
}
```

### 启动方式
```bash
# 启动监控服务
java -jar eon-visual/eon-monitor/target/*.jar

# 启动代码生成器
java -jar eon-visual/eon-codegen/target/*.jar

# 启动定时任务管理
java -jar eon-visual/eon-quartz/target/*.jar

# 通过网关访问
curl http://localhost:9999/eon-monitor/actuator/health
curl http://localhost:9999/eon-codegen/actuator/health
curl http://localhost:9999/eon-quartz/actuator/health
```

## 外部接口

### 监控服务接口
- **服务列表**: `GET /api/monitor/services` - 获取所有监控服务
- **服务详情**: `GET /api/monitor/services/{serviceId}` - 获取服务详情
- **系统指标**: `GET /api/monitor/metrics/system` - 获取系统指标
- **应用指标**: `GET /api/monitor/metrics/application` - 获取应用指标
- **告警规则**: `GET /api/monitor/alerts` - 获取告警规则
- **告警历史**: `GET /api/monitor/alerts/history` - 获取告警历史

### 代码生成器接口
- **数据源列表**: `GET /api/codegen/datasources` - 获取数据源列表
- **表结构**: `GET /api/codegen/tables` - 获取数据库表结构
- **生成代码**: `POST /api/codegen/generate` - 生成代码
- **模板列表**: `GET /api/codegen/templates` - 获取代码模板
- **下载代码**: `GET /api/codegen/download` - 下载生成的代码

### 定时任务管理接口
- **任务列表**: `GET /api/quartz/jobs` - 获取任务列表
- **任务详情**: `GET /api/quartz/jobs/{jobId}` - 获取任务详情
- **创建任务**: `POST /api/quartz/jobs` - 创建任务
- **更新任务**: `PUT /api/quartz/jobs/{jobId}` - 更新任务
- **删除任务**: `DELETE /api/quartz/jobs/{jobId}` - 删除任务
- **暂停任务**: `POST /api/quartz/jobs/{jobId}/pause` - 暂停任务
- **恢复任务**: `POST /api/quartz/jobs/{jobId}/resume` - 恢复任务
- **执行历史**: `GET /api/quartz/jobs/{jobId}/history` - 获取执行历史

## 关键依赖和配置

### 父级POM配置
```xml
<!-- eon-visual/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.eon</groupId>
        <artifactId>eon</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    
    <artifactId>eon-visual</artifactId>
    <packaging>pom</packaging>
    <name>eon-visual</name>
    <description>EON可视化工具集</description>
    
    <modules>
        <module>eon-monitor</module>
        <module>eon-codegen</module>
        <module>eon-quartz</module>
    </modules>
</project>
```

### 监控服务配置
```xml
<!-- eon-monitor/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-core</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

### 监控服务应用配置
```yaml
# eon-monitor/src/main/resources/application.yml
server:
  port: 5001

spring:
  application:
    name: eon-monitor
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.eon.visual.monitor: DEBUG
```

### 代码生成器配置
```xml
<!-- eon-codegen/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-freemarker</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-compress</artifactId>
    </dependency>
</dependencies>
```

### 定时任务配置
```xml
<!-- eon-quartz/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-quartz</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>
</dependencies>
```

## 数据模型

### 监控服务数据模型
```java
// 服务监控信息
public class ServiceMonitor {
    private String serviceId;
    private String serviceName;
    private String serviceAddress;
    private String status;
    private Long lastUpdateTime;
    private Map<String, Object> metrics;
}

// 系统指标
public class SystemMetrics {
    private String serviceId;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double diskUsage;
    private Long timestamp;
}

// 告警规则
public class AlertRule {
    private Long id;
    private String serviceId;
    private String metricName;
    private String operator;
    private Double threshold;
    private String level;
    private String description;
    private Boolean enabled;
}
```

### 代码生成器数据模型
```java
// 数据表信息
public class TableInfo {
    private String tableName;
    private String tableComment;
    private List<ColumnInfo> columns;
    private List<IndexInfo> indexes;
}

// 列信息
public class ColumnInfo {
    private String columnName;
    private String columnType;
    private String columnComment;
    private Boolean isPrimaryKey;
    private Boolean isNullable;
    private String defaultValue;
}

// 生成配置
public class GenerateConfig {
    private String tableName;
    private String packageName;
    private String moduleName;
    private String author;
    private Boolean removePrefix;
    private List<String> templates;
}
```

### 定时任务数据模型
```java
// 任务信息
public class JobInfo {
    private Long id;
    private String jobName;
    private String jobGroup;
    private String jobClass;
    private String cronExpression;
    private String description;
    private String status;
    private Date startTime;
    private Date endTime;
    private Map<String, Object> jobData;
}

// 任务执行历史
public class JobExecution {
    private Long id;
    private Long jobId;
    private String jobName;
    private String jobGroup;
    private Date fireTime;
    private Date runTime;
    private Integer runTimeMs;
    private String status;
    private String result;
    private String exception;
}
```

## 测试和质量

### 健康检查
```bash
# 检查监控服务
curl http://localhost:5001/actuator/health
curl http://localhost:9999/eon-monitor/actuator/health

# 检查代码生成器
curl http://localhost:5002/actuator/health
curl http://localhost:9999/eon-codegen/actuator/health

# 检查定时任务管理
curl http://localhost:5007/actuator/health
curl http://localhost:9999/eon-quartz/actuator/health
```

### 接口测试
```bash
# 监控服务测试
curl -X GET "http://localhost:5001/api/monitor/services"
curl -X GET "http://localhost:5001/api/monitor/metrics/system"

# 代码生成器测试
curl -X GET "http://localhost:5002/api/codegen/tables"
curl -X POST "http://localhost:5002/api/codegen/generate" \
  -H "Content-Type: application/json" \
  -d '{"tableName":"user","packageName":"com.eon.demo","author":"admin"}'

# 定时任务测试
curl -X GET "http://localhost:5007/api/quartz/jobs"
curl -X POST "http://localhost:5007/api/quartz/jobs" \
  -H "Content-Type: application/json" \
  -d '{"jobName":"testJob","jobGroup":"DEFAULT","jobClass":"com.eon.demo.TestJob","cronExpression":"0 0/1 * * * ?"}'
```

## 常见问题

### Q: 如何集成新的监控指标？
A: 实现自定义的MetricsCollector，注册到Spring的Metrics体系中。

### Q: 如何自定义代码模板？
A: 在代码生成器中添加新的模板文件，配置模板参数和生成逻辑。

### Q: 如何处理定时任务的异常？
A: 实现JobListener，监听任务执行事件，记录异常信息并发送告警。

### Q: 如何扩展监控面板？
A: 集成Grafana或自定义前端面板，通过API获取监控数据。

## 相关文件列表

### 监控服务文件
- `eon-visual/eon-monitor/pom.xml` - 监控服务Maven配置
- `eon-visual/eon-monitor/src/main/java/com/eon/visual/monitor/EonMonitorApplication.java` - 监控服务启动类
- `eon-visual/eon-monitor/src/main/resources/application.yml` - 监控服务配置
- `eon-visual/eon-monitor/src/main/java/com/eon/visual/monitor/controller/MonitorController.java` - 监控控制器
- `eon-visual/eon-monitor/src/main/java/com/eon/visual/monitor/service/MonitorService.java` - 监控服务接口

### 代码生成器文件
- `eon-visual/eon-codegen/pom.xml` - 代码生成器Maven配置
- `eon-visual/eon-codegen/src/main/java/com/eon/visual/codegen/EonCodegenApplication.java` - 代码生成器启动类
- `eon-visual/eon-codegen/src/main/resources/application.yml` - 代码生成器配置
- `eon-visual/eon-codegen/src/main/java/com/eon/visual/codegen/controller/CodegenController.java` - 代码生成控制器
- `eon-visual/eon-codegen/src/main/java/com/eon/visual/codegen/service/CodegenService.java` - 代码生成服务

### 定时任务管理文件
- `eon-visual/eon-quartz/pom.xml` - 定时任务管理Maven配置
- `eon-visual/eon-quartz/src/main/java/com/eon/visual/quartz/EonQuartzApplication.java` - 定时任务管理启动类
- `eon-visual/eon-quartz/src/main/resources/application.yml` - 定时任务管理配置
- `eon-visual/eon-quartz/src/main/java/com/eon/visual/quartz/controller/QuartzController.java` - 定时任务控制器
- `eon-visual/eon-quartz/src/main/java/com/eon/visual/quartz/service/QuartzService.java` - 定时任务服务

## 扩展指南

### 添加新的可视化工具
1. 在eon-visual下创建新的子模块
2. 实现相应的业务逻辑
3. 配置启动类和依赖
4. 集成到整体架构中

### 增强监控功能
1. 集成更多监控指标（JVM、数据库、缓存等）
2. 添加告警通知机制（邮件、短信、钉钉等）
3. 实现分布式链路追踪
4. 添加性能分析功能

### 扩展代码生成器
1. 支持更多代码模板（Vue、React、Go等）
2. 集成数据库设计工具
3. 添加代码质量检查
4. 支持批量生成和项目管理

### 增强定时任务功能
1. 支持任务依赖关系
2. 添加任务调度策略
3. 实现任务集群支持
4. 集成任务日志分析

---

**更新时间**: 2025-09-16 10:40:53  
**文档版本**: v1.0.0  
**维护状态**: 持续更新中