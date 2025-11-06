package com.example.analyzer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Java class in the analyzed codebase
 */
public class ClassInfo {
    private String id;
    private String serviceId;
    private String packageName;
    private String className;
    private String qualifiedName;
    private ClassType type;
    private List<String> annotations;
    private String filePath;
    private List<ClassDependency> dependencies;

    public ClassInfo() {
        this.annotations = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }

    public ClassInfo(String id, String serviceId, String qualifiedName) {
        this();
        this.id = id;
        this.serviceId = serviceId;
        this.qualifiedName = qualifiedName;
        parseQualifiedName(qualifiedName);
    }

    private void parseQualifiedName(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot > 0) {
            this.packageName = qualifiedName.substring(0, lastDot);
            this.className = qualifiedName.substring(lastDot + 1);
        } else {
            this.className = qualifiedName;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
        parseQualifiedName(qualifiedName);
    }

    public ClassType getType() {
        return type;
    }

    public void setType(ClassType type) {
        this.type = type;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<ClassDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<ClassDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(ClassDependency dependency) {
        this.dependencies.add(dependency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassInfo classInfo = (ClassInfo) o;
        return Objects.equals(id, classInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "id='" + id + '\'' +
                ", qualifiedName='" + qualifiedName + '\'' +
                ", type=" + type +
                '}';
    }
}
