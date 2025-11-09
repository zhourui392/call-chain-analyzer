package com.example.analyzer.analyzer;

import com.example.analyzer.model.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Analyzes class-level dependencies (field injection, constructor injection)
 */
public class ClassDependencyAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(ClassDependencyAnalyzer.class);

    /**
     * Analyze a single class for its dependencies
     */
    public List<ClassDependency> analyzeDependencies(CompilationUnit cu, ClassOrInterfaceDeclaration classDecl) {
        List<ClassDependency> dependencies = new ArrayList<>();

        // Analyze field injections
        classDecl.getFields().forEach(field -> {
            List<ClassDependency> fieldDeps = analyzeFieldInjection(field);
            dependencies.addAll(fieldDeps);
        });

        // Analyze constructor injections
        classDecl.getConstructors().forEach(constructor -> {
            List<ClassDependency> constructorDeps = analyzeConstructorInjection(constructor);
            dependencies.addAll(constructorDeps);
        });

        return dependencies;
    }

    /**
     * Analyze field injection dependencies
     */
    private List<ClassDependency> analyzeFieldInjection(FieldDeclaration field) {
        List<ClassDependency> dependencies = new ArrayList<>();

        field.getVariables().forEach(variable -> {
            InjectionType injectionType = detectInjectionType(field);
            if (injectionType != InjectionType.NONE) {
                ClassDependency dependency = new ClassDependency();
                dependency.setFieldName(variable.getNameAsString());
                dependency.setInjectionType(injectionType);

                try {
                    String typeName = variable.getType().asString();
                    dependency.setTargetQualifiedName(typeName);

                    // Determine scope
                    if (injectionType == InjectionType.DUBBO_REFERENCE) {
                        dependency.setScope(DependencyScope.RPC);
                        dependency.setInterfaceName(typeName);
                    } else {
                        dependency.setScope(DependencyScope.INTERNAL);
                    }

                    dependencies.add(dependency);
                    logger.debug("Found field dependency: {} -> {} ({})",
                            variable.getNameAsString(), typeName, injectionType);
                } catch (Exception e) {
                    logger.warn("Failed to resolve field type: {}", e.getMessage());
                }
            }
        });

        return dependencies;
    }

    /**
     * Analyze constructor injection dependencies
     */
    private List<ClassDependency> analyzeConstructorInjection(ConstructorDeclaration constructor) {
        List<ClassDependency> dependencies = new ArrayList<>();

        // Check if constructor has parameters (likely injection)
        if (constructor.getParameters().isEmpty()) {
            return dependencies;
        }

        for (Parameter param : constructor.getParameters()) {
            ClassDependency dependency = new ClassDependency();
            dependency.setFieldName(param.getNameAsString());
            dependency.setInjectionType(InjectionType.CONSTRUCTOR);
            dependency.setScope(DependencyScope.INTERNAL);

            try {
                String typeName = param.getType().asString();
                dependency.setTargetQualifiedName(typeName);
                dependencies.add(dependency);
                logger.debug("Found constructor dependency: {} -> {}",
                        param.getNameAsString(), typeName);
            } catch (Exception e) {
                logger.warn("Failed to resolve constructor parameter type: {}", e.getMessage());
            }
        }

        return dependencies;
    }

    /**
     * Detect injection type from field annotations
     */
    private InjectionType detectInjectionType(FieldDeclaration field) {
        for (AnnotationExpr annotation : field.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            switch (annotationName) {
                case "Autowired":
                    return InjectionType.AUTOWIRED;
                case "Resource":
                    return InjectionType.RESOURCE;
                case "Inject":
                    return InjectionType.INJECT;
                case "DubboReference":
                case "Reference":  // Dubbo 2.x uses @Reference
                    return InjectionType.DUBBO_REFERENCE;
            }
        }
        return InjectionType.NONE;
    }

    /**
     * Determine class type from annotations
     */
    public ClassType determineClassType(ClassOrInterfaceDeclaration classDecl) {
        if (classDecl.isInterface()) {
            return ClassType.INTERFACE;
        }

        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            // Support both simple and fully-qualified annotation names
            String name = annotationName.contains(".")
                    ? annotationName.substring(annotationName.lastIndexOf('.') + 1)
                    : annotationName;
            switch (name) {
                case "RestController":
                case "Controller":
                    return ClassType.CONTROLLER;
                case "Service":
                    return ClassType.SERVICE;
                case "Repository":
                    return ClassType.REPOSITORY;
                case "Component":
                    return ClassType.COMPONENT;
                case "Configuration":
                    return ClassType.CONFIGURATION;
                case "DubboService":
                    return ClassType.DUBBO_SERVICE;
            }
        }

        return ClassType.PLAIN_CLASS;
    }

    /**
     * Extract all annotations from a class
     */
    public List<String> extractAnnotations(ClassOrInterfaceDeclaration classDecl) {
        List<String> annotations = new ArrayList<>();
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            annotations.add("@" + annotation.toString());
        }
        return annotations;
    }
}
