package com.example.analyzer.model;

import java.util.Objects;

/**
 * Represents a dependency between two classes (field injection, constructor injection, etc.)
 */
public class ClassDependency {
    private String targetClassId;
    private String targetQualifiedName;
    private String fieldName;
    private InjectionType injectionType;
    private DependencyScope scope;
    private String targetService;  // For RPC calls
    private String interfaceName;  // For Dubbo interface

    public ClassDependency() {
    }

    public ClassDependency(String targetQualifiedName, String fieldName, InjectionType injectionType, DependencyScope scope) {
        this.targetQualifiedName = targetQualifiedName;
        this.fieldName = fieldName;
        this.injectionType = injectionType;
        this.scope = scope;
    }

    // Getters and Setters
    public String getTargetClassId() {
        return targetClassId;
    }

    public void setTargetClassId(String targetClassId) {
        this.targetClassId = targetClassId;
    }

    public String getTargetQualifiedName() {
        return targetQualifiedName;
    }

    public void setTargetQualifiedName(String targetQualifiedName) {
        this.targetQualifiedName = targetQualifiedName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public InjectionType getInjectionType() {
        return injectionType;
    }

    public void setInjectionType(InjectionType injectionType) {
        this.injectionType = injectionType;
    }

    public DependencyScope getScope() {
        return scope;
    }

    public void setScope(DependencyScope scope) {
        this.scope = scope;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassDependency that = (ClassDependency) o;
        return Objects.equals(targetQualifiedName, that.targetQualifiedName) &&
                Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetQualifiedName, fieldName);
    }

    @Override
    public String toString() {
        return "ClassDependency{" +
                "targetQualifiedName='" + targetQualifiedName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", injectionType=" + injectionType +
                ", scope=" + scope +
                '}';
    }
}
