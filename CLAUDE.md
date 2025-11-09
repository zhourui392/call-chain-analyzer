# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Call Chain Analyzer is a static analysis tool that analyzes Spring Boot + Dubbo microservice systems to trace method call chains. It uses JavaParser for AST parsing and outputs structured JSON suitable for graph database import (e.g., Neo4j).

**Core Purpose**: Trace complete call chains from HTTP endpoints through internal method calls and across RPC boundaries (Dubbo) in multi-service architectures.

## Build and Run Commands

### Build
```bash
# Clean and build JAR with dependencies
mvn clean package

# The executable JAR is generated at:
# target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

### Run Analysis
```bash
# Analyze single service
java -jar target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --service ./test-project/user-service \
  --output result.json

# Analyze multiple services (comma-separated)
java -jar target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --services ./service1,./service2,./service3 \
  --output chains.json

# Auto-discover services from parent directory
java -jar target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --services-dir ./services \
  --recursive \
  --output chains.json

# Pretty print JSON output
java -jar target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --service ./test-project/user-service \
  --output result.json \
  --pretty
```

### Test with Sample Project
```bash
# Run against the included test project
java -jar target/call-chain-analyzer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
  --service ./test-project/user-service \
  --output test-result.json
```

## Architecture Overview

### Analysis Pipeline

The tool follows a five-stage analysis pipeline:

1. **Service Discovery** (`ProjectScanner`)
   - Scans directories for Maven projects (pom.xml) or standard src/main/java structures
   - Extracts service metadata: groupId, artifactId, version, base package
   - Supports recursive discovery under parent directories

2. **Class-Level Analysis** (`ClassDependencyAnalyzer`)
   - Identifies class types via annotations: @RestController, @Service, @Repository, @Component, @Configuration, @DubboService
   - Analyzes field injection: @Autowired, @Resource, @Inject
   - Analyzes constructor injection patterns
   - Detects Dubbo RPC dependencies: @DubboReference, @Reference

3. **Method-Level Analysis** (`MethodCallAnalyzer`)
   - Extracts all methods with signatures, parameters, return types, annotations
   - Identifies HTTP endpoints: @GetMapping, @PostMapping, @PutMapping, @DeleteMapping, @RequestMapping
   - Parses method bodies to find all method call expressions

4. **Call Resolution** (`MethodCallAnalyzer` + `CallChainEngine`)
   - Resolves internal method calls by matching field names to dependencies
   - Distinguishes between same-class calls (this.method()) and field-based calls (service.method())
   - Identifies cross-service RPC calls via Dubbo reference fields
   - Uses best-effort matching: qualified name → simple name → endsWith matching

5. **Call Chain Construction** (`CallChainEngine`)
   - Starts from HTTP entry points (controller methods)
   - Recursively traces method calls up to depth 20 with cycle detection
   - Tracks service boundaries to identify cross-service chains
   - Outputs complete call graphs with depth levels

### Core Components

**Main.java** (`com.example.analyzer.Main`)
- Entry point with CLI argument parsing
- Orchestrates ProjectScanner → CallChainEngine → JsonExporter flow
- Handles --service, --services, --services-dir, --recursive, --output, --pretty flags

**ProjectScanner.java** (`com.example.analyzer.core.ProjectScanner`)
- Service directory discovery and validation
- POM.xml parsing for Maven metadata
- Base package detection from src/main/java structure
- Java file enumeration

**ClassDependencyAnalyzer.java** (`com.example.analyzer.analyzer.ClassDependencyAnalyzer`)
- Class type classification (CONTROLLER, SERVICE, REPOSITORY, COMPONENT, CONFIGURATION, DUBBO_SERVICE, INTERFACE, PLAIN_CLASS)
- Dependency injection pattern recognition
- Field vs constructor injection differentiation
- Internal vs RPC dependency scope determination

**MethodCallAnalyzer.java** (`com.example.analyzer.analyzer.MethodCallAnalyzer`)
- Method metadata extraction
- HTTP endpoint path extraction (combines @RequestMapping on class + method)
- Method call AST traversal using JavaParser Visitor pattern
- Target method resolution with fallback strategies

**CallChainEngine.java** (`com.example.analyzer.core.CallChainEngine`)
- Multi-service analysis orchestration
- Entry point identification (HTTP endpoints in controllers)
- Recursive call chain building with visited set for cycle prevention
- Cross-service chain detection

**JsonExporter.java** (`com.example.analyzer.exporter.JsonExporter`)
- Structured JSON output with metadata
- Pretty-print support via Jackson ObjectMapper
- Graph-database-friendly format

### Data Model

Key model classes in `com.example.analyzer.model`:

- **ServiceInfo**: Service metadata (id, name, artifactId, version, rootPath, basePackage)
- **ClassInfo**: Class metadata with dependencies (id, serviceId, qualifiedName, type, annotations, dependencies)
- **ClassDependency**: Injection relationship (targetQualifiedName, fieldName, injectionType, scope)
- **MethodInfo**: Method signature and metadata (id, classId, methodName, signature, returnType, parameters, annotations)
- **MethodCall**: Call relationship (id, sourceMethodId, targetMethodId, callType, crossService)
- **CallChain**: Complete call trace from entry point (entryPoint, chain nodes, maxDepth, crossService, involvedServices)
- **AnalysisResult**: Aggregated results with metadata (services, classes, methods, methodCalls, callChains)

### Important Patterns

**Dependency Injection Recognition**
- Field injection: Look for @Autowired/@Resource/@Inject/@DubboReference on fields
- Constructor injection: Any constructor with parameters is considered injection
- RPC scope: @DubboReference and @Reference indicate cross-service dependencies

**Method Call Resolution Strategy**
1. If call has no scope (e.g., `method()`): Same class
2. If scope is "this": Same class
3. If scope matches field name: Look up field's dependency target class
4. If scope looks like static class name: Match against all known classes
5. Match method by name + argument count; fallback to name-only if unique

**Call Type Classification**
- INTERNAL_METHOD_CALL: Same-service call
- RPC_METHOD_CALL: Cross-service call via Dubbo reference

**Entry Point Detection**
- Must be in a CONTROLLER class
- Must have @GetMapping/@PostMapping/@PutMapping/@DeleteMapping/@PatchMapping/@RequestMapping annotation

## Current Limitations (MVP)

1. **Symbol Resolution**: Basic implementation; may not resolve complex types or generics
2. **Generics**: Limited generic type inference
3. **Reflection**: No support for reflection or dynamic proxy analysis
4. **Lambda Expressions**: Limited lambda call tracing
5. **Configuration**: Does not parse application.yml or application.properties
6. **Cross-Service Matching**: Dubbo RPC calls identified but not matched to target service implementations
7. **HTTP Clients**: RestTemplate, FeignClient calls not yet tracked
8. **MyBatis**: Database mapper calls not analyzed
9. **Message Queues**: RabbitMQ, Kafka producers/consumers not tracked

## Development Notes

### Adding New Annotation Support

To support new Spring/Dubbo annotations:
1. Add to `ClassDependencyAnalyzer.determineClassType()` for class-level annotations (line 138-167)
2. Add to `ClassDependencyAnalyzer.detectInjectionType()` for field-level injection (line 117-133)
3. Add to `MethodCallAnalyzer.isHttpEndpoint()` for endpoint detection (line 214-226)

### Extending Call Type Detection

To track new types of calls (HTTP clients, message queues):
1. Add new `CallType` enum values in `CallType.java`
2. Extend `MethodCallAnalyzer.analyzeMethodCalls()` visitor logic (line 74-206)
3. Add field-based detection similar to Dubbo reference pattern

### Output Format

The JSON output is designed for Neo4j import with nodes (services, classes, methods) and edges (dependencies, method calls, call chains). The structure includes:
- `metadata`: Analysis summary stats
- `services`: Service nodes with IDs
- `classes`: Class nodes with serviceId references
- `methods`: Method nodes with classId references
- `methodCalls`: Call edges with sourceMethodId → targetMethodId
- `callChains`: Complete traces from HTTP endpoints

### Java Version

The project targets Java 8 (1.8) for broad compatibility. Keep this in mind when adding new features.
