package com.example.analyzer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a method in a Java class
 */
public class MethodInfo {
    private String id;
    private String classId;
    private String methodName;
    private String signature;
    private String returnType;
    private List<MethodParameter> parameters;
    private List<String> annotations;
    private int lineStart;
    private int lineEnd;

    public MethodInfo() {
        this.parameters = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    public MethodInfo(String id, String classId, String methodName, String signature) {
        this();
        this.id = id;
        this.classId = classId;
        this.methodName = methodName;
        this.signature = signature;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<MethodParameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(MethodParameter parameter) {
        this.parameters.add(parameter);
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

    public int getLineStart() {
        return lineStart;
    }

    public void setLineStart(int lineStart) {
        this.lineStart = lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "id='" + id + '\'' +
                ", methodName='" + methodName + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
