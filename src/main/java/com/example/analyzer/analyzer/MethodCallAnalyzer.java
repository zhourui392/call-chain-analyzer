package com.example.analyzer.analyzer;

import com.example.analyzer.model.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Analyzes method-level call relationships
 */
public class MethodCallAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(MethodCallAnalyzer.class);

    /**
     * Extract all methods from a class
     */
    public List<MethodInfo> extractMethods(ClassOrInterfaceDeclaration classDecl, String classId) {
        List<MethodInfo> methods = new ArrayList<>();

        classDecl.getMethods().forEach(method -> {
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setId(UUID.randomUUID().toString());
            methodInfo.setClassId(classId);
            methodInfo.setMethodName(method.getNameAsString());
            methodInfo.setSignature(method.getDeclarationAsString(false, false, false));

            // Set return type
            methodInfo.setReturnType(method.getType().asString());

            // Extract parameters
            int paramIndex = 0;
            for (com.github.javaparser.ast.body.Parameter param : method.getParameters()) {
                MethodParameter methodParam = new MethodParameter(
                        param.getNameAsString(),
                        param.getType().asString(),
                        paramIndex++
                );
                methodInfo.addParameter(methodParam);
            }

            // Extract annotations
            for (AnnotationExpr annotation : method.getAnnotations()) {
                methodInfo.addAnnotation("@" + annotation.toString());
            }

            // Set line numbers
            method.getBegin().ifPresent(pos -> methodInfo.setLineStart(pos.line));
            method.getEnd().ifPresent(pos -> methodInfo.setLineEnd(pos.line));

            methods.add(methodInfo);
        });

        return methods;
    }

    /**
     * Analyze method calls within a method
     */
    public List<MethodCall> analyzeMethodCalls(MethodDeclaration method,
                                                 String sourceMethodId,
                                                 ClassInfo sourceClass,
                                                 AnalysisResult analysisResult) {
        List<MethodCall> methodCalls = new ArrayList<>();

        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr call, Void arg) {
                try {
                    MethodCall methodCall = new MethodCall();
                    methodCall.setId(UUID.randomUUID().toString());
                    methodCall.setSourceMethodId(sourceMethodId);
                    methodCall.setCallerExpression(call.toString());

                    call.getBegin().ifPresent(pos -> methodCall.setSourceLineNumber(pos.line));

                    // Try to determine call type
                    String calledMethodName = call.getNameAsString();

                    // Check if it's a call on a Dubbo reference field
                    if (call.getScope().isPresent()) {
                        String scopeName = call.getScope().get().toString();

                        // Look for field in dependencies
                        boolean isDubboCall = sourceClass.getDependencies().stream()
                                .anyMatch(dep -> dep.getFieldName().equals(scopeName)
                                        && dep.getInjectionType() == InjectionType.DUBBO_REFERENCE);

                        if (isDubboCall) {
                            methodCall.setCallType(CallType.RPC_METHOD_CALL);
                            methodCall.setCrossService(true);

                            // Find target service (simplified - would need more logic in real impl)
                            sourceClass.getDependencies().stream()
                                    .filter(dep -> dep.getFieldName().equals(scopeName))
                                    .findFirst()
                                    .ifPresent(dep -> {
                                        methodCall.setTargetQualifiedMethod(
                                                dep.getTargetQualifiedName() + "." + calledMethodName);
                                    });
                        } else {
                            methodCall.setCallType(CallType.INTERNAL_METHOD_CALL);
                            methodCall.setCrossService(false);
                        }
                    } else {
                        // No scope - likely internal method call
                        methodCall.setCallType(CallType.INTERNAL_METHOD_CALL);
                        methodCall.setCrossService(false);
                    }

                    methodCalls.add(methodCall);
                    logger.debug("Found method call: {} at line {}",
                            call.getNameAsString(),
                            call.getBegin().map(pos -> pos.line).orElse(-1));

                } catch (Exception e) {
                    logger.warn("Failed to analyze method call {}: {}",
                            call.getNameAsString(), e.getMessage());
                }

                super.visit(call, arg);
            }
        }, null);

        return methodCalls;
    }

    /**
     * Check if a method is an HTTP endpoint
     */
    public boolean isHttpEndpoint(MethodInfo method) {
        for (String annotation : method.getAnnotations()) {
            if (annotation.contains("@GetMapping") ||
                annotation.contains("@PostMapping") ||
                annotation.contains("@PutMapping") ||
                annotation.contains("@DeleteMapping") ||
                annotation.contains("@PatchMapping") ||
                annotation.contains("@RequestMapping")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract HTTP endpoint path from annotations
     */
    public String extractHttpEndpoint(MethodInfo method, ClassInfo classInfo) {
        String classPath = "";
        String methodPath = "";

        // Get class-level path
        for (String annotation : classInfo.getAnnotations()) {
            if (annotation.contains("@RequestMapping")) {
                classPath = extractPathFromAnnotation(annotation);
            }
        }

        // Get method-level path
        for (String annotation : method.getAnnotations()) {
            if (annotation.contains("Mapping")) {
                methodPath = extractPathFromAnnotation(annotation);

                // Also extract HTTP method
                String httpMethod = "GET";  // default
                if (annotation.contains("@PostMapping")) httpMethod = "POST";
                else if (annotation.contains("@PutMapping")) httpMethod = "PUT";
                else if (annotation.contains("@DeleteMapping")) httpMethod = "DELETE";
                else if (annotation.contains("@PatchMapping")) httpMethod = "PATCH";

                return httpMethod + " " + classPath + methodPath;
            }
        }

        return classPath + methodPath;
    }

    /**
     * Extract path value from annotation string
     */
    private String extractPathFromAnnotation(String annotation) {
        // Simple extraction - in real implementation, would parse annotation properly
        if (annotation.contains("value") || annotation.contains("\"")) {
            int start = annotation.indexOf('"');
            int end = annotation.lastIndexOf('"');
            if (start >= 0 && end > start) {
                return annotation.substring(start + 1, end);
            }
        }
        return "";
    }
}
