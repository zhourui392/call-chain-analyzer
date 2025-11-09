# 五服务 Dubbo 微服务测试项目设计方案

**版本:** 1.0
**日期:** 2025-11-09
**目标:** 创建一个包含五个微服务的复杂 Dubbo 测试项目，用于验证调用链分析器的跨服务追踪能力

## 业务场景：电商订单系统

模拟一个真实的电商订单处理流程，涉及用户、商品、订单、支付、通知等多个业务域。

## 服务架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway / Client                     │
│                       (HTTP REST Endpoints)                      │
└────────────┬────────────────────────────────────────────────────┘
             │
             ▼
    ┌────────────────┐
    │  user-service  │ (用户服务 - 入口服务)
    │  @RestController│
    └────┬───┬───┬───┘
         │   │   │
    ┌────┘   │   └────┐
    │        │        │
    ▼        ▼        ▼
┌──────┐  ┌──────┐  ┌───────────┐
│order │  │product│  │notification│
│service│  │service│  │  service  │
└──┬───┘  └───┘  └───────────────┘
   │          │
   │      ┌───┘
   │      │
   ▼      ▼
┌──────────┐
│ payment  │
│ service  │
└──────────┘
```

## 服务详细设计

### 1. user-service (用户服务) - 入口服务

**职责:**
- 提供用户管理功能
- 作为入口服务，对外提供 HTTP REST API
- 调用其他服务完成复杂业务流程

**依赖的 Dubbo 服务:**
- `OrderService` - 查询用户订单
- `ProductService` - 查询用户浏览记录推荐商品
- `NotificationService` - 发送用户通知

**对外提供的 Dubbo 服务:**
- `UserService` - 提供用户信息查询

**HTTP 端点:**
- `GET /api/users/{id}` - 获取用户详情（含订单信息）
- `POST /api/users/{id}/orders` - 用户下单（触发跨服务调用链）
- `GET /api/users/{id}/recommendations` - 获取商品推荐

**类结构:**
```
com.example.user
├── controller
│   └── UserController.java          (@RestController)
├── service
│   ├── UserService.java              (@Service, 本地服务)
│   └── UserFacadeService.java        (协调多个 RPC 调用)
├── api
│   └── UserInfoService.java          (Dubbo 接口)
├── provider
│   └── UserInfoServiceImpl.java      (@DubboService)
├── repository
│   └── UserRepository.java           (@Repository)
└── model
    └── User.java                     (实体类)
```

### 2. order-service (订单服务) - 核心业务服务

**职责:**
- 订单创建、查询、更新
- 协调商品、支付、通知服务

**依赖的 Dubbo 服务:**
- `ProductService` - 检查商品库存和价格
- `PaymentService` - 创建支付订单
- `NotificationService` - 发送订单通知
- `UserInfoService` - 获取用户信息

**对外提供的 Dubbo 服务:**
- `OrderService` - 订单管理接口

**类结构:**
```
com.example.order
├── api
│   └── OrderService.java             (Dubbo 接口)
├── service
│   ├── OrderServiceImpl.java         (@DubboService)
│   └── OrderProcessService.java      (订单处理逻辑)
├── repository
│   └── OrderRepository.java          (@Repository)
└── model
    └── Order.java                    (实体类)
```

### 3. product-service (商品服务) - 基础服务

**职责:**
- 商品信息管理
- 库存检查和扣减
- 商品推荐

**依赖的 Dubbo 服务:**
- 无（基础服务，不依赖其他业务服务）

**对外提供的 Dubbo 服务:**
- `ProductService` - 商品查询和库存管理
- `RecommendationService` - 商品推荐

**类结构:**
```
com.example.product
├── api
│   ├── ProductService.java           (Dubbo 接口)
│   └── RecommendationService.java    (Dubbo 接口)
├── service
│   ├── ProductServiceImpl.java       (@DubboService)
│   ├── RecommendationServiceImpl.java(@DubboService)
│   └── InventoryService.java         (库存管理)
├── repository
│   ├── ProductRepository.java        (@Repository)
│   └── InventoryRepository.java      (@Repository)
└── model
    ├── Product.java                  (实体类)
    └── Inventory.java                (实体类)
```

### 4. payment-service (支付服务) - 核心业务服务

**职责:**
- 支付订单创建
- 支付状态查询
- 支付成功后回调通知

**依赖的 Dubbo 服务:**
- `NotificationService` - 发送支付结果通知
- `OrderService` - 更新订单支付状态

**对外提供的 Dubbo 服务:**
- `PaymentService` - 支付管理接口

**类结构:**
```
com.example.payment
├── api
│   └── PaymentService.java           (Dubbo 接口)
├── service
│   ├── PaymentServiceImpl.java       (@DubboService)
│   └── PaymentProcessService.java    (支付处理逻辑)
├── repository
│   └── PaymentRepository.java        (@Repository)
└── model
    └── Payment.java                  (实体类)
```

### 5. notification-service (通知服务) - 支撑服务

**职责:**
- 发送各类通知（邮件、短信、站内信）
- 通知记录存储

**依赖的 Dubbo 服务:**
- `UserInfoService` - 获取用户联系方式

**对外提供的 Dubbo 服务:**
- `NotificationService` - 通知发送接口

**类结构:**
```
com.example.notification
├── api
│   └── NotificationService.java      (Dubbo 接口)
├── service
│   ├── NotificationServiceImpl.java  (@DubboService)
│   ├── EmailService.java             (邮件发送)
│   └── SmsService.java               (短信发送)
├── repository
│   └── NotificationRepository.java   (@Repository)
└── model
    └── Notification.java             (实体类)
```

## 关键调用链场景

### 场景 1: 用户下单流程

**入口:** `POST /api/users/{userId}/orders`

**调用链:**
```
UserController.createOrder()                    (user-service)
└─> UserFacadeService.placeOrder()              (user-service)
    ├─> ProductService.checkInventory()         [RPC] (product-service)
    │   └─> InventoryService.checkStock()       (product-service)
    │       └─> InventoryRepository.findByProductId() (product-service)
    │
    ├─> OrderService.createOrder()              [RPC] (order-service)
    │   ├─> UserInfoService.getUserInfo()       [RPC] (user-service)
    │   │   └─> UserRepository.findById()       (user-service)
    │   │
    │   ├─> ProductService.getProductInfo()     [RPC] (product-service)
    │   │   └─> ProductRepository.findById()    (product-service)
    │   │
    │   ├─> OrderRepository.save()              (order-service)
    │   │
    │   └─> PaymentService.createPayment()      [RPC] (payment-service)
    │       ├─> PaymentRepository.save()        (payment-service)
    │       │
    │       └─> NotificationService.sendNotification() [RPC] (notification-service)
    │           ├─> UserInfoService.getUserContacts() [RPC] (user-service)
    │           │   └─> UserRepository.findById() (user-service)
    │           │
    │           ├─> EmailService.send()         (notification-service)
    │           │
    │           └─> NotificationRepository.save() (notification-service)
    │
    └─> ProductService.decreaseInventory()      [RPC] (product-service)
        └─> InventoryService.decrease()         (product-service)
            └─> InventoryRepository.update()    (product-service)
```

**调用链特点:**
- 最大深度: 6-7 层
- 跨服务调用: 10+ 次
- 涉及所有 5 个服务
- 包含循环依赖场景（user ↔ notification ↔ user）

### 场景 2: 获取用户详情

**入口:** `GET /api/users/{id}`

**调用链:**
```
UserController.getUser()                        (user-service)
└─> UserService.getUserWithOrders()             (user-service)
    ├─> UserRepository.findById()               (user-service)
    │
    └─> OrderService.getOrdersByUserId()        [RPC] (order-service)
        └─> OrderRepository.findByUserId()      (order-service)
```

**调用链特点:**
- 最大深度: 3 层
- 跨服务调用: 1 次
- 涉及 2 个服务

### 场景 3: 商品推荐

**入口:** `GET /api/users/{id}/recommendations`

**调用链:**
```
UserController.getRecommendations()             (user-service)
└─> UserFacadeService.getRecommendations()      (user-service)
    ├─> OrderService.getUserOrderHistory()      [RPC] (order-service)
    │   └─> OrderRepository.findRecentByUserId() (order-service)
    │
    └─> RecommendationService.recommend()       [RPC] (product-service)
        ├─> ProductRepository.findByCategory()  (product-service)
        │
        └─> InventoryService.filterInStock()    (product-service)
            └─> InventoryRepository.findInStock() (product-service)
```

**调用链特点:**
- 最大深度: 4 层
- 跨服务调用: 2 次
- 涉及 3 个服务

## 技术实现细节

### Maven 项目结构
```
test-project-complex/
├── user-service/
│   ├── pom.xml
│   └── src/main/java/com/example/user/...
├── order-service/
│   ├── pom.xml
│   └── src/main/java/com/example/order/...
├── product-service/
│   ├── pom.xml
│   └── src/main/java/com/example/product/...
├── payment-service/
│   ├── pom.xml
│   └── src/main/java/com/example/payment/...
└── notification-service/
    ├── pom.xml
    └── src/main/java/com/example/notification/...
```

### Dubbo 配置

所有服务使用注解配置：
- `@DubboService(version = "1.0.0", group = "ecommerce")`
- `@DubboReference(version = "1.0.0", group = "ecommerce")`

### 依赖关系矩阵

| 服务 ↓ / 依赖 → | user-service | order-service | product-service | payment-service | notification-service |
|---|:---:|:---:|:---:|:---:|:---:|
| **user-service** | - | ✓ | ✓ | - | ✓ |
| **order-service** | ✓ | - | ✓ | ✓ | ✓ |
| **product-service** | - | - | - | - | - |
| **payment-service** | - | ✓ | - | - | ✓ |
| **notification-service** | ✓ | - | - | - | - |

**说明:**
- product-service: 基础服务，无外部依赖
- notification-service: 支撑服务，只依赖 user-service
- payment-service: 依赖 notification-service 和 order-service
- order-service: 核心服务，依赖最多（4 个服务）
- user-service: 入口服务，依赖 3 个服务

## 预期分析结果

### 统计指标
- **服务数:** 5
- **类数:** ~25 (每个服务 4-6 个类)
- **方法数:** ~100+
- **Dubbo 接口数:** 7
- **Dubbo 实现类数:** 8
- **HTTP 端点数:** 3
- **调用链数:** 3 (对应 3 个 HTTP 端点)
- **跨服务调用数:** 15+
- **最大调用深度:** 7 层
- **最复杂调用链:** 用户下单流程（涉及所有 5 个服务）

### 测试验证点

1. **接口解析能力:**
   - ✓ 正确映射所有 Dubbo 接口到实现类
   - ✓ 处理多个实现类的情况（如 product-service 提供 2 个接口）

2. **跨服务追踪能力:**
   - ✓ 追踪简单跨服务调用（2 个服务）
   - ✓ 追踪复杂跨服务调用（5 个服务）
   - ✓ 追踪多层嵌套调用（7 层深度）

3. **循环依赖处理:**
   - ✓ 检测并正确处理循环调用（user ↔ notification）
   - ✓ 避免死循环（通过访问集合）

4. **服务依赖分析:**
   - ✓ 生成完整的服务依赖关系图
   - ✓ 识别基础服务、核心服务、支撑服务

5. **性能测试:**
   - ✓ 分析 5 个服务的性能表现
   - ✓ JSON 输出文件大小和结构

## 实施步骤

### 第一阶段: 基础服务实现
1. 实现 product-service（无外部依赖，最简单）
2. 实现 user-service 的 UserInfoService（被其他服务依赖）

### 第二阶段: 支撑服务实现
3. 实现 notification-service（依赖 user-service）

### 第三阶段: 核心业务服务实现
4. 实现 payment-service（依赖 notification-service）
5. 实现 order-service（依赖最多，最复杂）

### 第四阶段: 入口服务实现
6. 完善 user-service 的 Controller 和 Facade 层

### 第五阶段: 测试和验证
7. 使用调用链分析器分析所有服务
8. 生成分析报告和可视化
9. 验证所有预期测试点

## 输出文件

1. **test-project-complex/** - 五服务源代码
2. **complex-analysis-result.json** - 分析结果 JSON
3. **COMPLEX_ANALYSIS_REPORT.md** - 详细分析报告
4. **service-dependency-graph.txt** - 服务依赖关系图

## 成功标准

- ✅ 所有 5 个服务编译成功
- ✅ 调用链分析器成功分析所有服务
- ✅ 识别出所有 3 个完整调用链
- ✅ 正确追踪最深 7 层的调用链
- ✅ 正确处理 15+ 次跨服务 RPC 调用
- ✅ 生成包含所有服务关系的 JSON 输出
- ✅ 验证报告展示清晰的调用链关系

## 时间估算

- 设计文档编写: 30 分钟 ✅
- 代码实现: 60-90 分钟
- 分析和验证: 20 分钟
- 报告生成: 15 分钟
- **总计:** ~2-3 小时
