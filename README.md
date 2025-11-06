# Call Chain Analyzer

静态分析工具，用于分析 Spring Boot + Dubbo 微服务系统的调用链路。

## 功能特性

- ✅ **类级别依赖分析**: 识别类之间的依赖关系
- ✅ **方法级别调用分析**: 追踪方法间的调用关系
- ✅ **多服务支持**: 同时分析多个微服务
- ✅ **依赖注入识别**: 支持 @Autowired, @Resource, @Inject, 构造器注入
- ✅ **Dubbo RPC 识别**: 识别 @DubboReference 跨服务调用
- ✅ **调用链追踪**: 从 HTTP 入口点追踪完整调用链
- ✅ **JSON 输出**: 结构化数据输出，便于后续处理
- ✅ **图数据库友好**: 数据模型设计适配 Neo4j

## 技术栈

- **JavaParser**: Java 源码 AST 解析
- **Jackson**: JSON 序列化/反序列化
- **SLF4J + Logback**: 日志
- **Maven**: 构建工具

## 快速开始

### 1. 构建项目

```bash
cd call-chain-analyzer
mvn clean package
```

### 2. 运行分析

```bash
# 分析单个服务
java -jar target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --service ./test-project/user-service \
  --output result.json

# 分析多个服务
java -jar target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --services ./user-service,./order-service,./payment-service \
  --output chains.json \
  --pretty
```

### 3. 查看结果

```bash
cat result.json
```

## 使用示例

### 命令行参数

```
Usage:
  java -jar call-chain-analyzer.jar [OPTIONS] <service-path>...

Options:
  -s, --service <path>       单个服务目录路径
  --services <paths>         多个服务路径（逗号分隔）
  -o, --output <file>        输出 JSON 文件路径 (默认: analysis-result.json)
  --pretty                   格式化 JSON 输出
  -h, --help                 显示帮助信息
```

### 示例项目

项目包含一个测试用例在 `test-project/user-service`：

```bash
# 分析测试项目
java -jar target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --service ./test-project/user-service \
  --output test-result.json
```

## 输出格式

### JSON 结构

```json
{
  "metadata": {
    "analysisTime": "2025-11-06T10:00:00",
    "projectName": "multi-service-analysis",
    "totalServices": 2,
    "totalClasses": 50,
    "totalMethods": 200
  },
  "services": [
    {
      "id": "service-001",
      "name": "user-service",
      "artifactId": "user-service",
      "version": "1.0.0"
    }
  ],
  "classes": [
    {
      "id": "class-001",
      "serviceId": "service-001",
      "qualifiedName": "com.example.user.controller.UserController",
      "type": "CONTROLLER",
      "annotations": ["@RestController", "@RequestMapping(\"/api/users\")"],
      "dependencies": [
        {
          "targetQualifiedName": "com.example.user.service.UserService",
          "fieldName": "userService",
          "injectionType": "AUTOWIRED",
          "scope": "INTERNAL"
        }
      ]
    }
  ],
  "methods": [
    {
      "id": "method-001",
      "classId": "class-001",
      "methodName": "getUser",
      "signature": "getUser(Long)",
      "returnType": "User",
      "annotations": ["@GetMapping(\"/{id}\")"]
    }
  ],
  "methodCalls": [
    {
      "id": "call-001",
      "sourceMethodId": "method-001",
      "targetMethodId": "method-002",
      "callType": "INTERNAL_METHOD_CALL",
      "crossService": false
    }
  ],
  "callChains": [
    {
      "id": "chain-001",
      "entryPoint": {
        "methodId": "method-001",
        "httpEndpoint": "GET /api/users/{id}"
      },
      "chain": [
        {"level": 0, "methodId": "method-001", "serviceId": "service-001"},
        {"level": 1, "methodId": "method-002", "serviceId": "service-001"}
      ],
      "maxDepth": 2,
      "crossService": false
    }
  ]
}
```

## 架构设计

### 核心组件

1. **ProjectScanner**: 扫描服务目录，识别服务信息
2. **ClassDependencyAnalyzer**: 分析类级别依赖（字段注入、构造器注入）
3. **MethodCallAnalyzer**: 分析方法级别调用
4. **CallChainEngine**: 构建完整调用链
5. **JsonExporter**: 导出 JSON 格式结果

### 分析流程

```
1. 扫描服务目录 → 发现所有服务
2. 解析 Java 文件 → 构建 AST
3. 分析类依赖 → 识别注入关系
4. 分析方法调用 → 追踪调用关系
5. 构建调用链 → 从入口点开始追踪
6. 导出结果 → 生成 JSON
```

## 支持的注解

### Spring 注解
- `@RestController` / `@Controller`
- `@Service`
- `@Repository`
- `@Component`
- `@Configuration`
- `@Autowired`
- `@Resource`
- `@Inject`
- `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping`
- `@RequestMapping`

### Dubbo 注解
- `@DubboService`
- `@DubboReference`
- `@Reference` (Dubbo 2.x)

## 限制和注意事项

当前 MVP 版本的限制：

1. **符号解析**: 基础实现，可能无法解析所有复杂类型
2. **泛型支持**: 有限的泛型类型推断
3. **反射调用**: 不支持反射和动态代理分析
4. **Lambda 表达式**: 有限的 lambda 调用追踪
5. **配置文件**: 不解析 application.yml 配置

## 后续计划

- [ ] 完整的符号解析（使用 SymbolSolver）
- [ ] 跨服务 Dubbo 调用链匹配
- [ ] HTTP 客户端调用分析（RestTemplate, FeignClient）
- [ ] MyBatis Mapper 调用分析
- [ ] 消息队列调用分析（RabbitMQ, Kafka）
- [ ] Neo4j 导出适配器
- [ ] 可视化界面
- [ ] 增量分析支持

## 迁移到图数据库

输出的 JSON 格式可以直接导入 Neo4j：

```cypher
// 创建服务节点
CREATE (s:Service {
  id: "service-001",
  name: "user-service"
})

// 创建类节点
CREATE (c:Class {
  id: "class-001",
  qualifiedName: "com.example.user.controller.UserController",
  type: "CONTROLLER"
})

// 创建依赖关系
MATCH (c1:Class {id: "class-001"})
MATCH (c2:Class {id: "class-002"})
CREATE (c1)-[:DEPENDS_ON {
  injectionType: "AUTOWIRED",
  scope: "INTERNAL"
}]->(c2)
```

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可

MIT License
