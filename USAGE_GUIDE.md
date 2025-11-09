# 调用链分析器 - 使用指南

## 快速开始

### 方式 1: 自动发现服务（推荐）⭐

**只需指定最外层目录，分析器会自动发现所有子服务：**

```bash
# 分析 test-project-complex 目录下的所有服务
java -jar call-chain-analyzer.jar --services-dir ./test-project-complex --output result.json
```

**自动发现的服务：**
- ✅ product-service
- ✅ user-service
- ✅ notification-service
- ✅ payment-service
- ✅ order-service

### 方式 2: 递归发现服务

**如果服务嵌套在多层目录中，使用 `--recursive` 选项：**

```bash
# 递归扫描所有子目录
java -jar call-chain-analyzer.jar --services-dir ./projects --recursive --output result.json
```

**目录结构示例：**
```
projects/
├── backend/
│   ├── user-service/         ← 会被发现
│   └── order-service/        ← 会被发现
└── microservices/
    ├── payment-service/      ← 会被发现
    └── notification-service/ ← 会被发现
```

### 方式 3: 手动指定服务（传统方式）

```bash
# 方式 3a: 逗号分隔的服务列表
java -jar call-chain-analyzer.jar \
  --services ./user-service,./order-service,./product-service \
  --output result.json

# 方式 3b: 多次使用 --service 参数
java -jar call-chain-analyzer.jar \
  --service ./user-service \
  --service ./order-service \
  --service ./product-service \
  --output result.json
```

---

## 命令行选项

### 核心选项

| 选项 | 简写 | 说明 | 示例 |
|------|------|------|------|
| `--services-dir <dir>` | `-d` | 自动发现服务目录 | `-d ./microservices` |
| `--recursive` | `-r` | 递归扫描子目录 | `-r` |
| `--service <path>` | `-s` | 指定单个服务路径 | `-s ./user-service` |
| `--services <paths>` | - | 逗号分隔的服务列表 | `--services ./a,./b` |
| `--output <file>` | `-o` | 输出文件路径 | `-o result.json` |
| `--pretty` | - | 格式化 JSON 输出 | `--pretty` |
| `--help` | `-h` | 显示帮助信息 | `-h` |

### 服务发现规则

**分析器认为一个目录是服务，如果它满足以下任一条件：**

1. ✅ 包含 `pom.xml` 文件
2. ✅ 包含标准 Maven 目录结构 `src/main/java`

**示例：**
```
my-service/
├── pom.xml              ← 标记为服务
└── src/main/java/       ← 标记为服务
    └── com/example/...
```

---

## 实际使用示例

### 示例 1: 分析电商系统

```bash
# 项目结构
ecommerce/
├── user-service/
├── order-service/
├── product-service/
├── payment-service/
└── notification-service/

# 一条命令分析所有服务
java -jar call-chain-analyzer.jar \
  --services-dir ./ecommerce \
  --output ecommerce-chains.json
```

**输出：**
```
Discovering services under: /path/to/ecommerce (recursive=false)
Discovered 5 service directories
Analyzing services: [user-service, order-service, ...]
Services analyzed: 5
Classes found: 30
Methods found: 122
Call chains: 3
Results saved to: ecommerce-chains.json
```

### 示例 2: 分析嵌套项目

```bash
# 项目结构
company-project/
├── backend/
│   ├── core-services/
│   │   ├── user-service/
│   │   └── auth-service/
│   └── business-services/
│       ├── order-service/
│       └── payment-service/
└── frontend/
    └── web-app/

# 使用递归模式
java -jar call-chain-analyzer.jar \
  --services-dir ./company-project \
  --recursive \
  --output company-analysis.json
```

**会发现：**
- ✅ user-service
- ✅ auth-service
- ✅ order-service
- ✅ payment-service
- ❌ web-app (不是 Java 服务)

### 示例 3: 只分析特定服务

```bash
# 只分析用户和订单服务
java -jar call-chain-analyzer.jar \
  --services ./user-service,./order-service \
  --output user-order-chains.json
```

### 示例 4: Windows PowerShell

```powershell
# Windows 路径使用反斜杠
java -jar call-chain-analyzer.jar `
  --services-dir .\microservices `
  --recursive `
  --output analysis.json
```

---

## 对比：旧方式 vs 新方式

### ❌ 旧方式（繁琐）

```bash
# 需要手动列出每个服务
java -jar analyzer.jar \
  --services ./test-project-complex/product-service,\
./test-project-complex/user-service,\
./test-project-complex/notification-service,\
./test-project-complex/payment-service,\
./test-project-complex/order-service \
  --output result.json
```

**问题：**
- 需要知道所有服务名称
- 命令行太长
- 添加新服务需要修改命令

### ✅ 新方式（简洁）

```bash
# 只需指定父目录
java -jar analyzer.jar \
  --services-dir ./test-project-complex \
  --output result.json
```

**优势：**
- 命令简短
- 自动发现服务
- 添加新服务无需修改命令

---

## 高级用法

### 1. 结合查看工具

```bash
# 分析并立即查看结果
java -jar call-chain-analyzer.jar -d ./services -o result.json && \
python3 view_call_chains.py result.json
```

### 2. CI/CD 集成

```yaml
# .gitlab-ci.yml 示例
analyze-call-chains:
  stage: test
  script:
    - java -jar tools/call-chain-analyzer.jar
        --services-dir ./microservices
        --recursive
        --output call-chains.json
    - python3 tools/view_call_chains.py call-chains.json
  artifacts:
    paths:
      - call-chains.json
```

### 3. 脚本自动化

```bash
#!/bin/bash
# analyze-all.sh

SERVICES_DIR=${1:-./services}
OUTPUT_FILE=${2:-analysis-$(date +%Y%m%d).json}

echo "分析服务目录: $SERVICES_DIR"
echo "输出文件: $OUTPUT_FILE"

java -jar call-chain-analyzer.jar \
  --services-dir "$SERVICES_DIR" \
  --recursive \
  --output "$OUTPUT_FILE" \
  --pretty

if [ $? -eq 0 ]; then
  echo "✅ 分析成功！"
  python3 view_call_chains.py "$OUTPUT_FILE"
else
  echo "❌ 分析失败！"
  exit 1
fi
```

**使用：**
```bash
chmod +x analyze-all.sh
./analyze-all.sh ./microservices
```

---

## 常见问题

### Q1: 为什么某些目录没有被识别为服务？

**A:** 确保目录满足以下条件之一：
- 包含 `pom.xml` 文件
- 包含 `src/main/java` 目录

### Q2: 如何排除某些目录？

**A:** 当前版本会自动跳过不符合服务特征的目录。如果需要更精细的控制，使用 `--services` 手动指定。

### Q3: 递归模式会扫描多深？

**A:** 递归模式会扫描所有子目录层级，但只识别包含 `pom.xml` 或 `src/main/java` 的目录。

### Q4: 服务发现的顺序重要吗？

**A:** 不重要。分析器会自动处理服务间的依赖关系。

### Q5: 可以同时使用多种方式指定服务吗？

**A:** 可以！所有指定的服务会合并在一起分析：

```bash
java -jar analyzer.jar \
  --services-dir ./main-services \
  --service ./legacy-service \
  --services ./extra-service1,./extra-service2 \
  --output all-services.json
```

---

## 输出说明

### 控制台输出

```
Discovering services under: /path/to/services (recursive=false)
Discovered 5 service directories              ← 自动发现的服务数
=== Call Chain Analyzer ===
Analyzing services: [...]                     ← 将要分析的服务列表
Services analyzed: 5                          ← 分析的服务数
Classes found: 30                             ← 发现的类总数
Methods found: 122                            ← 发现的方法总数
Method calls: 129                             ← 方法调用总数
Call chains: 3                                ← 调用链数量
Results saved to: result.json                 ← 输出文件
```

### JSON 输出文件

```json
{
  "metadata": {
    "totalServices": 5,
    "totalClasses": 30,
    "totalMethods": 122,
    "analysisDate": "2025-11-09T15:27:17Z"
  },
  "services": [...],
  "classes": [...],
  "methods": [...],
  "callChains": [...]
}
```

---

## 性能建议

### 小型项目 (< 10 服务)
```bash
# 直接使用，无需特殊配置
java -jar analyzer.jar -d ./services -o result.json
```

### 中型项目 (10-50 服务)
```bash
# 建议增加 JVM 内存
java -Xmx2g -jar analyzer.jar -d ./services -o result.json
```

### 大型项目 (> 50 服务)
```bash
# 增加内存并启用并行GC
java -Xmx4g -XX:+UseParallelGC \
  -jar analyzer.jar -d ./services -o result.json
```

---

## 总结

### 最佳实践 ⭐

1. **优先使用 `--services-dir`** 自动发现服务
2. **嵌套项目使用 `--recursive`** 递归扫描
3. **结合可视化工具** 查看分析结果
4. **集成到 CI/CD** 持续监控调用链
5. **保存历史结果** 对比代码变更影响

### 推荐命令

```bash
# 最常用的命令（简洁版）
java -jar call-chain-analyzer.jar -d ./services -o chains.json

# 完整功能（详细版）
java -jar call-chain-analyzer.jar \
  --services-dir ./microservices \
  --recursive \
  --output analysis-$(date +%Y%m%d).json \
  --pretty
```

---

**工具版本:** v1.0.0
**最后更新:** 2025-11-09
