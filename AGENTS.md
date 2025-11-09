# Repository Guidelines

## 项目结构与模块组织
- `src/main/java/com/example/analyzer/core`：调用链引擎（入口封装、流程编排）
- `src/main/java/com/example/analyzer/analyzer`：类/方法分析器（依赖与调用解析）
- `src/main/java/com/example/analyzer/model`：领域模型（ClassInfo/MethodInfo/CallChain 等）
- `src/main/java/com/example/analyzer/exporter`：结果导出（JSON）
- `src/main/resources/logback.xml`：日志配置（SLF4J + Logback）
- `test-project/user-service`：示例待分析项目
- `pom.xml`：Maven 构建配置，主类 `com.example.analyzer.Main`

## 构建、测试与本地运行
- 构建：`mvn clean package`（生成 `target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar`）
- 运行（Windows 11 PowerShell 示例）：
  `java -jar target\call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar --service .\test-project\user-service --output result.json`
- 多服务分析：
  `java -jar target\...jar --services .\user-service,.\order-service --output chains.json --pretty`
- 测试：`mvn test`（已引入 JUnit 5 依赖；当前仓库未包含测试用例）

## 代码风格与命名规范
- 遵循 Alibaba-P3C 规范；建议 IDE 安装 Alibaba Java Coding Guidelines 插件。
- Java 8，缩进 4 空格；每行尽量不超过 120 列。
- 包名小写；类名 UpperCamelCase；方法/变量 lowerCamelCase；常量 UPPER_SNAKE_CASE。
- 统一使用 SLF4J 日志：`private static final Logger log = LoggerFactory.getLogger(...);`，避免 `System.out`。
- 公共接口/核心领域模型补充 Javadoc；方法入参做非空与前置条件校验。

## 测试规范
- 测试框架：JUnit 5（`org.junit.jupiter`）。
- 目录：`src/test/java`；命名：与被测类对应的 `*Test.java`。
- 覆盖重点：`ProjectScanner`、`ClassDependencyAnalyzer`、`MethodCallAnalyzer`、`CallChainEngine`、`JsonExporter` 的关键路径与边界条件。
- 运行：`mvn -q test`；必要时在 PR 中附上关键断言或样例输出片段。

## Commit 与 Pull Request
- Commit 规范：Conventional Commits（示例：`feat: support multi-service analysis`、`fix: resolve NPE in analyzer`、`docs: update README`）。
- PR 要求：
  - 清晰描述目的/影响范围与风险点；
  - 列出主要变更点与本地运行步骤；
  - 关联 Issue 编号；必要时附示例命令与生成的 JSON 片段/截图。

## Windows 11 提示
- 需安装 JDK 8+ 与 Maven；配置 `JAVA_HOME`、`MAVEN_HOME` 并加入 `PATH`。
- 优先使用英文无空格路径；命令中的路径分隔符使用 `\`。

