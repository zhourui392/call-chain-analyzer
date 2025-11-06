package com.example.analyzer.core;

import com.example.analyzer.analyzer.ClassDependencyAnalyzer;
import com.example.analyzer.analyzer.MethodCallAnalyzer;
import com.example.analyzer.model.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Main engine for analyzing call chains across services
 */
public class CallChainEngine {
    private static final Logger logger = LoggerFactory.getLogger(CallChainEngine.class);

    private final ProjectScanner projectScanner;
    private final ClassDependencyAnalyzer classDependencyAnalyzer;
    private final MethodCallAnalyzer methodCallAnalyzer;

    public CallChainEngine() {
        this.projectScanner = new ProjectScanner();
        this.classDependencyAnalyzer = new ClassDependencyAnalyzer();
        this.methodCallAnalyzer = new MethodCallAnalyzer();
    }

    /**
     * Analyze multiple services and build complete call chain graph
     */
    public AnalysisResult analyze(List<String> servicePaths) {
        logger.info("Starting analysis of {} services", servicePaths.size());

        AnalysisResult result = new AnalysisResult();
        result.getMetadata().setProjectName("multi-service-analysis");

        // Step 1: Scan all services
        List<ServiceInfo> services = projectScanner.scanServices(servicePaths);
        result.setServices(services);
        result.getMetadata().setTotalServices(services.size());
        logger.info("Discovered {} services", services.size());

        // Step 2: Analyze each service
        for (ServiceInfo service : services) {
            analyzeService(service, result);
        }

        // Step 3: Build call chains from entry points
        buildCallChains(result);

        // Step 4: Update metadata
        result.getMetadata().setTotalClasses(result.getClasses().size());
        result.getMetadata().setTotalMethods(result.getMethods().size());

        logger.info("Analysis complete: {} classes, {} methods, {} calls",
                result.getClasses().size(),
                result.getMethods().size(),
                result.getMethodCalls().size());

        return result;
    }

    /**
     * Analyze a single service
     */
    private void analyzeService(ServiceInfo service, AnalysisResult result) {
        logger.info("Analyzing service: {}", service.getName());

        List<Path> javaFiles = projectScanner.findJavaFiles(service);
        logger.info("Found {} Java files in {}", javaFiles.size(), service.getName());

        for (Path javaFile : javaFiles) {
            try {
                analyzeJavaFile(javaFile, service, result);
            } catch (Exception e) {
                logger.error("Failed to analyze file {}: {}", javaFile, e.getMessage(), e);
            }
        }
    }

    /**
     * Analyze a single Java file
     */
    private void analyzeJavaFile(Path javaFile, ServiceInfo service, AnalysisResult result) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            // Find all class declarations
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                try {
                    analyzeClass(cu, classDecl, service, javaFile, result);
                } catch (Exception e) {
                    logger.error("Failed to analyze class {}: {}",
                            classDecl.getNameAsString(), e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Failed to parse file {}: {}", javaFile, e.getMessage());
        }
    }

    /**
     * Analyze a single class
     */
    private void analyzeClass(CompilationUnit cu,
                              ClassOrInterfaceDeclaration classDecl,
                              ServiceInfo service,
                              Path javaFile,
                              AnalysisResult result) {

        // Create ClassInfo
        ClassInfo classInfo = new ClassInfo();
        classInfo.setId(UUID.randomUUID().toString());
        classInfo.setServiceId(service.getId());

        // Get qualified name
        String qualifiedName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString() + "." + classDecl.getNameAsString())
                .orElse(classDecl.getNameAsString());
        classInfo.setQualifiedName(qualifiedName);
        classInfo.setFilePath(javaFile.toString());

        // Determine class type
        ClassType classType = classDependencyAnalyzer.determineClassType(classDecl);
        classInfo.setType(classType);

        // Extract annotations
        List<String> annotations = classDependencyAnalyzer.extractAnnotations(classDecl);
        classInfo.setAnnotations(annotations);

        // Analyze dependencies
        List<ClassDependency> dependencies = classDependencyAnalyzer.analyzeDependencies(cu, classDecl);
        classInfo.setDependencies(dependencies);

        result.addClass(classInfo);
        logger.debug("Analyzed class: {} ({})", qualifiedName, classType);

        // Extract and analyze methods
        List<MethodInfo> methods = methodCallAnalyzer.extractMethods(classDecl, classInfo.getId());
        methods.forEach(result::addMethod);

        // Analyze method calls in each method
        classDecl.getMethods().forEach(methodDecl -> {
            String methodSignature = methodDecl.getDeclarationAsString(false, false, false);
            MethodInfo methodInfo = methods.stream()
                    .filter(m -> m.getSignature().equals(methodSignature))
                    .findFirst()
                    .orElse(null);

            if (methodInfo != null) {
                List<MethodCall> calls = methodCallAnalyzer.analyzeMethodCalls(
                        methodDecl, methodInfo.getId(), classInfo, result);
                calls.forEach(result::addMethodCall);
            }
        });
    }

    /**
     * Build call chains from entry points (Controllers)
     */
    private void buildCallChains(AnalysisResult result) {
        logger.info("Building call chains from entry points");

        int chainCount = 0;
        for (ClassInfo classInfo : result.getClasses()) {
            if (classInfo.getType() == ClassType.CONTROLLER) {
                for (MethodInfo method : result.getMethods()) {
                    if (method.getClassId().equals(classInfo.getId()) &&
                        methodCallAnalyzer.isHttpEndpoint(method)) {

                        CallChain chain = buildCallChain(method, classInfo, result);
                        if (chain != null) {
                            result.addCallChain(chain);
                            chainCount++;
                        }
                    }
                }
            }
        }

        logger.info("Built {} call chains", chainCount);
    }

    /**
     * Build a single call chain starting from an entry point
     */
    private CallChain buildCallChain(MethodInfo entryMethod, ClassInfo entryClass, AnalysisResult result) {
        CallChain chain = new CallChain(UUID.randomUUID().toString());

        // Create entry point
        CallChain.CallChainNode entryNode = new CallChain.CallChainNode(
                0, entryMethod.getId(), entryClass.getId(), entryClass.getServiceId());
        String httpEndpoint = methodCallAnalyzer.extractHttpEndpoint(entryMethod, entryClass);
        entryNode.setHttpEndpoint(httpEndpoint);
        chain.setEntryPoint(entryNode);
        chain.addNode(entryNode);
        chain.addInvolvedService(entryClass.getServiceId());

        // Recursively build chain
        buildCallChainRecursive(entryMethod.getId(), entryClass.getServiceId(), 1, chain, result, new HashSet<>());

        chain.setMaxDepth(chain.getChain().size());
        chain.setCrossService(chain.getInvolvedServices().size() > 1);

        return chain;
    }

    /**
     * Recursively build call chain
     */
    private void buildCallChainRecursive(String currentMethodId,
                                         String currentServiceId,
                                         int level,
                                         CallChain chain,
                                         AnalysisResult result,
                                         Set<String> visited) {
        // Prevent infinite recursion
        if (visited.contains(currentMethodId) || level > 20) {
            return;
        }
        visited.add(currentMethodId);

        // Find all calls from current method
        for (MethodCall call : result.getMethodCalls()) {
            if (call.getSourceMethodId().equals(currentMethodId)) {
                // For now, we only track calls we can resolve
                if (call.getTargetMethodId() != null) {
                    MethodInfo targetMethod = result.getMethodById(call.getTargetMethodId());
                    if (targetMethod != null) {
                        ClassInfo targetClass = result.getClassById(targetMethod.getClassId());
                        if (targetClass != null) {
                            CallChain.CallChainNode node = new CallChain.CallChainNode(
                                    level, targetMethod.getId(), targetClass.getId(), targetClass.getServiceId());
                            node.setCallType(call.getCallType());
                            chain.addNode(node);
                            chain.addInvolvedService(targetClass.getServiceId());

                            // Continue recursively
                            buildCallChainRecursive(targetMethod.getId(), targetClass.getServiceId(),
                                    level + 1, chain, result, visited);
                        }
                    }
                } else if (call.isCrossService()) {
                    // RPC call - might not be able to resolve target
                    logger.debug("Found cross-service RPC call: {}", call.getTargetQualifiedMethod());
                }
            }
        }
    }
}
