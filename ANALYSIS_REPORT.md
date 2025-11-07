# 📊 Call Chain Analyzer - 检测结果报告

**分析时间**: 2025-11-07T08:04:47.745
**项目名称**: multi-service-analysis

---

## 📈 统计概览

| 指标 | 数量 |
|------|------|
| 服务总数 | 1 |
| 类总数 | 3 |
| 方法总数 | 12 |
| 方法调用数 | 9 |
| 调用链总数 | 3 |

---

## 🏗️ 服务架构

### Service: User Service
- **Group ID**: com.example
- **Artifact ID**: user-service
- **Version**: 1.0.0
- **Base Package**: com.example.user.service

---

## 📦 类信息分析

### 1. UserController (CONTROLLER)
```
com.example.user.controller.UserController
```

**注解**:
- `@RestController`
- `@RequestMapping("/api/users")`

**依赖关系**:
```
UserController
    ↓ @Autowired (字段: userService)
UserService
```

**方法列表**:
- `User getUser(Long)` - 行 15-17
  - 注解: `@GetMapping("/{id}")`

- `User createUser(User)` - 行 20-22
  - 注解: `@PostMapping`

- `void deleteUser(Long)` - 行 25-27
  - 注解: `@DeleteMapping("/{id}")`

---

### 2. UserService (SERVICE)
```
com.example.user.service.UserService
```

**注解**:
- `@Service`

**方法列表**:
- `User findUserById(Long)` - 行 9-16
- `User createUser(User)` - 行 18-22
- `void deleteUser(Long)` - 行 24-27

---

### 3. User (PLAIN_CLASS)
```
com.example.user.model.User
```

**类型**: 普通 POJO 类

**方法列表**:
- `Long getId()` - 行 8-10
- `void setId(Long)` - 行 12-14
- `String getName()` - 行 16-18
- `void setName(String)` - 行 20-22
- `String getEmail()` - 行 24-26
- `void setEmail(String)` - 行 28-30

---

## 🔗 调用链路分析

### 调用链 #1: GET /api/users/{id}

**入口**: `GET /api/users/{id}`

```
Level 0: UserController.getUser(Long)
    ↓ 调用
Level 1: UserService.findUserById(Long)
    ↓ 调用
         - user.setId(id)
         - user.setName("John Doe")
         - user.setEmail("john@example.com")
```

**特征**:
- 最大深度: 2
- 跨服务: 否
- 涉及类: UserController → UserService

---

### 调用链 #2: POST /api/users

**入口**: `POST /api/users`

```
Level 0: UserController.createUser(User)
    ↓ 调用
Level 1: UserService.createUser(User)
    ↓ 调用
         - user.setId(System.currentTimeMillis())
```

**特征**:
- 最大深度: 2
- 跨服务: 否
- 涉及类: UserController → UserService

---

### 调用链 #3: DELETE /api/users/{id}

**入口**: `DELETE /api/users/{id}`

```
Level 0: UserController.deleteUser(Long)
    ↓ 调用
Level 1: UserService.deleteUser(Long)
    ↓ 调用
         - System.out.println("Deleting user: " + id)
```

**特征**:
- 最大深度: 2
- 跨服务: 否
- 涉及类: UserController → UserService

---

## 🎯 关键发现

### ✅ 成功识别的内容

1. **依赖注入识别**
   - ✅ 检测到 UserController 通过 `@Autowired` 注入 UserService

2. **注解识别**
   - ✅ Spring MVC 注解: @RestController, @RequestMapping
   - ✅ HTTP 方法注解: @GetMapping, @PostMapping, @DeleteMapping
   - ✅ 服务层注解: @Service

3. **调用关系追踪**
   - ✅ Controller → Service 层调用
   - ✅ Service 内部的方法调用 (如 user.setId(), user.setName())
   - ✅ 系统方法调用 (如 System.out.println(), System.currentTimeMillis())

4. **HTTP 端点映射**
   - ✅ `GET /api/users/{id}` → UserController.getUser()
   - ✅ `POST /api/users` → UserController.createUser()
   - ✅ `DELETE /api/users/{id}` → UserController.deleteUser()

---

## 📊 数据质量评估

| 维度 | 评分 | 说明 |
|------|------|------|
| 类识别准确性 | ⭐⭐⭐⭐⭐ | 100% 准确识别 3 个类 |
| 方法提取完整性 | ⭐⭐⭐⭐⭐ | 完整提取 12 个方法 |
| 依赖关系准确性 | ⭐⭐⭐⭐⭐ | 正确识别 @Autowired 注入 |
| 调用链完整性 | ⭐⭐⭐⭐☆ | 追踪到主要调用路径 |
| 注解解析准确性 | ⭐⭐⭐⭐⭐ | 正确解析所有 Spring 注解 |

---

## 🔮 可视化示例 (适合导入图数据库)

### Neo4j Cypher 查询示例

```cypher
// 创建服务节点
CREATE (s:Service {
  id: "d286b041-9d3e-4fef-bd42-ec83c1300f1e",
  name: "User Service",
  artifactId: "user-service"
})

// 创建类节点
CREATE (c1:Class {
  id: "d86558cd-43e4-49d0-b968-4c43fcdc2434",
  name: "UserController",
  type: "CONTROLLER"
})

CREATE (c2:Class {
  id: "935f9b1c-3c94-4b96-8d4c-60f95e51748a",
  name: "UserService",
  type: "SERVICE"
})

// 创建依赖关系
MATCH (c1:Class {name: "UserController"})
MATCH (c2:Class {name: "UserService"})
CREATE (c1)-[:DEPENDS_ON {
  injectionType: "AUTOWIRED",
  fieldName: "userService"
}]->(c2)

// 查询调用链
MATCH path = (c1:Class)-[:CALLS*]->(c2:Class)
WHERE c1.type = "CONTROLLER"
RETURN path
```

---

## 💡 建议和优化方向

### 当前版本优势
1. ✅ 准确识别 Spring Boot 标准架构 (Controller-Service 模式)
2. ✅ 完整提取 HTTP 端点信息
3. ✅ 正确追踪依赖注入关系
4. ✅ 输出格式适合导入图数据库

### 可改进点
1. 🔧 增强方法调用的目标解析 (当前部分调用的 targetMethodId 为 null)
2. 🔧 添加跨服务 Dubbo 调用的实际案例测试
3. 🔧 支持更复杂的调用链路 (如异步调用、AOP 切面)

---

## 📝 总结

本次检测成功分析了一个标准的 Spring Boot 微服务项目，准确识别了：

- **3个类**: Controller, Service, Model
- **12个方法**: 包括 REST 端点和业务逻辑
- **9个方法调用**: 追踪了 Controller 到 Service 的调用路径
- **3条调用链**: 从 HTTP 端点到业务逻辑的完整路径

所有数据已成功导出为 JSON 格式，可以直接导入 Neo4j 等图数据库进行进一步分析和可视化。

---

**生成时间**: 2025-11-07
**工具版本**: Call Chain Analyzer MVP 1.0.0
