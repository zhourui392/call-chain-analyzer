# 五服务调用链详细设计

## 调用链图例说明

```
符号说明:
└─>     内部方法调用
[RPC]   Dubbo RPC 跨服务调用
(服务名) 方法所属服务
│       调用链分支
├─>     并行调用分支
```

---

## 场景 1: 用户下单流程 (最复杂)

### 业务流程

用户下单 → 检查库存 → 创建订单 → 创建支付 → 发送通知 → 扣减库存

### HTTP 入口

```
POST /api/users/{userId}/orders
Body: {
  "productId": "P001",
  "quantity": 2,
  "address": "上海市浦东新区"
}
```

### 完整调用链（7层深度）

```
Level 0 (HTTP Entry Point)
│
├─ UserController.createOrder(userId, orderRequest)
│  位置: com.example.user.controller.UserController
│  服务: user-service
│  注解: @PostMapping("/api/users/{userId}/orders")
│
└──> Level 1
     │
     ├─ UserFacadeService.placeOrder(userId, productId, quantity, address)
     │  位置: com.example.user.service.UserFacadeService
     │  服务: user-service
     │  注解: @Service
     │  职责: 协调多个RPC调用，编排业务流程
     │
     ├──> Level 2 - Branch 1: 检查库存
     │    │
     │    ├─ ProductService.checkInventory(productId, quantity) [RPC]
     │    │  接口: com.example.product.api.ProductService
     │    │  实现: com.example.product.service.ProductServiceImpl
     │    │  服务: product-service
     │    │  注解: @DubboService(version = "1.0.0", group = "ecommerce")
     │    │
     │    └──> Level 3
     │         │
     │         ├─ InventoryService.checkStock(productId, quantity)
     │         │  位置: com.example.product.service.InventoryService
     │         │  服务: product-service
     │         │  注解: @Service
     │         │
     │         └──> Level 4
     │              │
     │              └─ InventoryRepository.findByProductId(productId)
     │                 位置: com.example.product.repository.InventoryRepository
     │                 服务: product-service
     │                 注解: @Repository
     │                 返回: Inventory { stock: 100, reserved: 20, available: 80 }
     │
     ├──> Level 2 - Branch 2: 创建订单
     │    │
     │    ├─ OrderService.createOrder(userId, productId, quantity, address) [RPC]
     │    │  接口: com.example.order.api.OrderService
     │    │  实现: com.example.order.service.OrderServiceImpl
     │    │  服务: order-service
     │    │  注解: @DubboService(version = "1.0.0", group = "ecommerce")
     │    │
     │    └──> Level 3 - OrderService内部流程
     │         │
     │         ├──> Branch 2.1: 获取用户信息
     │         │    │
     │         │    ├─ UserInfoService.getUserInfo(userId) [RPC]
     │         │    │  接口: com.example.user.api.UserInfoService
     │         │    │  实现: com.example.user.provider.UserInfoServiceImpl
     │         │    │  服务: user-service (循环调用回用户服务!)
     │         │    │  注解: @DubboService(version = "1.0.0", group = "ecommerce")
     │         │    │
     │         │    └──> Level 4
     │         │         │
     │         │         └─ UserRepository.findById(userId)
     │         │            位置: com.example.user.repository.UserRepository
     │         │            服务: user-service
     │         │            注解: @Repository
     │         │            返回: User { id: 123, name: "张三", level: "VIP" }
     │         │
     │         ├──> Branch 2.2: 获取商品信息
     │         │    │
     │         │    ├─ ProductService.getProductInfo(productId) [RPC]
     │         │    │  接口: com.example.product.api.ProductService
     │         │    │  实现: com.example.product.service.ProductServiceImpl
     │         │    │  服务: product-service
     │         │    │
     │         │    └──> Level 4
     │         │         │
     │         │         └─ ProductRepository.findById(productId)
     │         │            位置: com.example.product.repository.ProductRepository
     │         │            服务: product-service
     │         │            注解: @Repository
     │         │            返回: Product { id: "P001", name: "iPhone 15", price: 5999 }
     │         │
     │         ├──> Branch 2.3: 保存订单
     │         │    │
     │         │    └─ OrderRepository.save(order)
     │         │       位置: com.example.order.repository.OrderRepository
     │         │       服务: order-service
     │         │       注解: @Repository
     │         │       返回: Order { id: "O20231109001", totalAmount: 11998 }
     │         │
     │         └──> Branch 2.4: 创建支付
     │              │
     │              ├─ PaymentService.createPayment(orderId, amount) [RPC]
     │              │  接口: com.example.payment.api.PaymentService
     │              │  实现: com.example.payment.service.PaymentServiceImpl
     │              │  服务: payment-service
     │              │  注解: @DubboService(version = "1.0.0", group = "ecommerce")
     │              │
     │              └──> Level 4 - PaymentService内部流程
     │                   │
     │                   ├──> Branch 2.4.1: 保存支付记录
     │                   │    │
     │                   │    └─ PaymentRepository.save(payment)
     │                   │       位置: com.example.payment.repository.PaymentRepository
     │                   │       服务: payment-service
     │                   │       注解: @Repository
     │                   │       返回: Payment { id: "PAY001", status: "PENDING" }
     │                   │
     │                   └──> Branch 2.4.2: 发送支付通知
     │                        │
     │                        ├─ NotificationService.sendNotification(userId, type, content) [RPC]
     │                        │  接口: com.example.notification.api.NotificationService
     │                        │  实现: com.example.notification.service.NotificationServiceImpl
     │                        │  服务: notification-service
     │                        │  注解: @DubboService(version = "1.0.0", group = "ecommerce")
     │                        │
     │                        └──> Level 5 - NotificationService内部流程
     │                             │
     │                             ├──> Branch 2.4.2.1: 获取用户联系方式
     │                             │    │
     │                             │    ├─ UserInfoService.getUserContacts(userId) [RPC]
     │                             │    │  接口: com.example.user.api.UserInfoService
     │                             │    │  实现: com.example.user.provider.UserInfoServiceImpl
     │                             │    │  服务: user-service (再次循环调用!)
     │                             │    │
     │                             │    └──> Level 6
     │                             │         │
     │                             │         └─ UserRepository.findById(userId)
     │                             │            位置: com.example.user.repository.UserRepository
     │                             │            服务: user-service
     │                             │            注解: @Repository
     │                             │            返回: User { email: "zhangsan@example.com", phone: "13800138000" }
     │                             │
     │                             ├──> Branch 2.4.2.2: 发送邮件
     │                             │    │
     │                             │    └─ EmailService.send(email, subject, content)
     │                             │       位置: com.example.notification.service.EmailService
     │                             │       服务: notification-service
     │                             │       注解: @Service
     │                             │       操作: 调用第三方邮件服务发送邮件
     │                             │
     │                             └──> Branch 2.4.2.3: 保存通知记录
     │                                  │
     │                                  └─ NotificationRepository.save(notification)
     │                                     位置: com.example.notification.repository.NotificationRepository
     │                                     服务: notification-service
     │                                     注解: @Repository
     │                                     返回: Notification { id: "N001", status: "SENT" }
     │
     └──> Level 2 - Branch 3: 扣减库存
          │
          ├─ ProductService.decreaseInventory(productId, quantity) [RPC]
          │  接口: com.example.product.api.ProductService
          │  实现: com.example.product.service.ProductServiceImpl
          │  服务: product-service
          │
          └──> Level 3
               │
               ├─ InventoryService.decrease(productId, quantity)
               │  位置: com.example.product.service.InventoryService
               │  服务: product-service
               │  注解: @Service
               │
               └──> Level 4
                    │
                    └─ InventoryRepository.update(productId, newStock)
                       位置: com.example.product.repository.InventoryRepository
                       服务: product-service
                       注解: @Repository
                       返回: Inventory { stock: 98, reserved: 20, available: 78 }
```

### 调用链统计

| 指标 | 数值 | 说明 |
|------|------|------|
| **最大深度** | 7 层 | Level 0 (HTTP) → Level 6 (UserRepository) |
| **RPC 调用次数** | 10 次 | 所有标记 [RPC] 的调用 |
| **涉及服务数** | 5 个 | 所有服务都参与 |
| **服务调用路径** | user → product → order → user → product → payment → notification → user | 包含循环 |
| **循环依赖次数** | 2 次 | order → user, notification → user |
| **并行调用分支** | 3 个 | 检查库存、创建订单、扣减库存 |
| **Repository 调用** | 8 次 | 数据库操作 |

### 服务间调用关系（此场景）

```
user-service
├─> product-service (3次)
│   ├─ checkInventory()
│   ├─ getProductInfo()
│   └─ decreaseInventory()
│
└─> order-service (1次)
    ├─> user-service (1次) ← 循环
    │   └─ getUserInfo()
    │
    ├─> product-service (1次)
    │   └─ getProductInfo()
    │
    └─> payment-service (1次)
        └─> notification-service (1次)
            └─> user-service (1次) ← 循环
                └─ getUserContacts()
```

---

## 场景 2: 获取用户详情 (简单场景)

### 业务流程

查询用户 → 查询用户订单 → 返回用户详情

### HTTP 入口

```
GET /api/users/{id}
```

### 完整调用链（3层深度）

```
Level 0 (HTTP Entry Point)
│
├─ UserController.getUser(id)
│  位置: com.example.user.controller.UserController
│  服务: user-service
│  注解: @GetMapping("/api/users/{id}")
│
└──> Level 1
     │
     ├─ UserService.getUserWithOrders(id)
     │  位置: com.example.user.service.UserService
     │  服务: user-service
     │  注解: @Service
     │
     ├──> Level 2 - Branch 1: 查询用户基本信息
     │    │
     │    └─ UserRepository.findById(id)
     │       位置: com.example.user.repository.UserRepository
     │       服务: user-service
     │       注解: @Repository
     │       返回: User { id: 123, name: "张三", email: "zhangsan@example.com" }
     │
     └──> Level 2 - Branch 2: 查询用户订单
          │
          ├─ OrderService.getOrdersByUserId(id) [RPC]
          │  接口: com.example.order.api.OrderService
          │  实现: com.example.order.service.OrderServiceImpl
          │  服务: order-service
          │  注解: @DubboService(version = "1.0.0", group = "ecommerce")
          │
          └──> Level 3
               │
               └─ OrderRepository.findByUserId(id)
                  位置: com.example.order.repository.OrderRepository
                  服务: order-service
                  注解: @Repository
                  返回: List<Order> [
                    { id: "O001", status: "COMPLETED", amount: 5999 },
                    { id: "O002", status: "PENDING", amount: 11998 }
                  ]
```

### 调用链统计

| 指标 | 数值 | 说明 |
|------|------|------|
| **最大深度** | 3 层 | Level 0 (HTTP) → Level 3 (OrderRepository) |
| **RPC 调用次数** | 1 次 | OrderService.getOrdersByUserId() |
| **涉及服务数** | 2 个 | user-service, order-service |
| **服务调用路径** | user → order | 简单线性调用 |
| **循环依赖次数** | 0 次 | 无循环 |
| **并行调用分支** | 2 个 | 查询用户、查询订单（可并行） |
| **Repository 调用** | 2 次 | UserRepository, OrderRepository |

---

## 场景 3: 商品推荐 (中等复杂度)

### 业务流程

获取用户订单历史 → 分析购买偏好 → 推荐商品 → 过滤有库存商品

### HTTP 入口

```
GET /api/users/{id}/recommendations
```

### 完整调用链（5层深度）

```
Level 0 (HTTP Entry Point)
│
├─ UserController.getRecommendations(id)
│  位置: com.example.user.controller.UserController
│  服务: user-service
│  注解: @GetMapping("/api/users/{id}/recommendations")
│
└──> Level 1
     │
     ├─ UserFacadeService.getRecommendations(id)
     │  位置: com.example.user.service.UserFacadeService
     │  服务: user-service
     │  注解: @Service
     │
     ├──> Level 2 - Branch 1: 获取订单历史
     │    │
     │    ├─ OrderService.getUserOrderHistory(id) [RPC]
     │    │  接口: com.example.order.api.OrderService
     │    │  实现: com.example.order.service.OrderServiceImpl
     │    │  服务: order-service
     │    │  注解: @DubboService(version = "1.0.0", group = "ecommerce")
     │    │
     │    └──> Level 3
     │         │
     │         └─ OrderRepository.findRecentByUserId(id, limit = 10)
     │            位置: com.example.order.repository.OrderRepository
     │            服务: order-service
     │            注解: @Repository
     │            返回: List<Order> [最近10个订单，包含商品分类信息]
     │
     └──> Level 2 - Branch 2: 生成推荐
          │
          ├─ RecommendationService.recommend(userId, orderHistory) [RPC]
          │  接口: com.example.product.api.RecommendationService
          │  实现: com.example.product.service.RecommendationServiceImpl
          │  服务: product-service
          │  注解: @DubboService(version = "1.0.0", group = "ecommerce")
          │
          └──> Level 3 - RecommendationService内部流程
               │
               ├──> Branch 2.1: 查询同类商品
               │    │
               │    └─ ProductRepository.findByCategory(category)
               │       位置: com.example.product.repository.ProductRepository
               │       服务: product-service
               │       注解: @Repository
               │       返回: List<Product> [同类商品列表]
               │
               └──> Branch 2.2: 过滤有库存商品
                    │
                    ├─ InventoryService.filterInStock(productIds)
                    │  位置: com.example.product.service.InventoryService
                    │  服务: product-service
                    │  注解: @Service
                    │
                    └──> Level 4
                         │
                         └─ InventoryRepository.findInStock(productIds)
                            位置: com.example.product.repository.InventoryRepository
                            服务: product-service
                            注解: @Repository
                            返回: List<Product> [有库存的推荐商品]
```

### 调用链统计

| 指标 | 数值 | 说明 |
|------|------|------|
| **最大深度** | 5 层 | Level 0 (HTTP) → Level 4 (InventoryRepository) |
| **RPC 调用次数** | 2 次 | OrderService, RecommendationService |
| **涉及服务数** | 3 个 | user-service, order-service, product-service |
| **服务调用路径** | user → order, user → product | 两条独立路径 |
| **循环依赖次数** | 0 次 | 无循环 |
| **并行调用分支** | 2 个 | 订单历史、商品推荐（必须串行） |
| **Repository 调用** | 3 次 | OrderRepository, ProductRepository, InventoryRepository |

---

## 三个场景对比总结

| 场景 | 深度 | RPC次数 | 服务数 | 复杂度 | 循环依赖 | 关键特点 |
|------|------|---------|--------|--------|----------|----------|
| **场景1: 下单** | 7层 | 10次 | 5个 | 高 | 2次 | 涉及所有服务，多层嵌套 |
| **场景2: 用户详情** | 3层 | 1次 | 2个 | 低 | 0次 | 简单查询，适合基础测试 |
| **场景3: 推荐** | 5层 | 2次 | 3个 | 中 | 0次 | 多分支，适合并行分析 |

## 调用链分析器测试要点

### 1. 简单场景验证（场景2）
- ✓ 基本跨服务追踪
- ✓ RPC 接口解析
- ✓ Repository 层识别

### 2. 中等场景验证（场景3）
- ✓ 多RPC调用处理
- ✓ 同服务多接口（ProductService, RecommendationService）
- ✓ 内部服务调用链

### 3. 复杂场景验证（场景1）
- ✓ 深层嵌套追踪（7层）
- ✓ 循环依赖处理（防止死循环）
- ✓ 多分支并行调用
- ✓ 所有服务协同

### 4. 边界情况
- ✓ 同一个服务被多次RPC调用（user-service被3次调用）
- ✓ 一个方法内多次RPC调用（UserFacadeService.placeOrder）
- ✓ RPC调用返回调用方服务（循环）

## 实现优先级

**Phase 1: 基础服务（无依赖）**
1. product-service (ProductService, RecommendationService, InventoryService)

**Phase 2: 支撑服务**
2. user-service 的 UserInfoService（被其他服务依赖）
3. notification-service

**Phase 3: 业务服务**
4. payment-service
5. order-service（依赖最多，最后实现）

**Phase 4: 入口服务**
6. user-service 的 Controller 和 UserFacadeService

这样的实现顺序确保每个服务实现时，其依赖的服务已经完成。
