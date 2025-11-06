package com.example.analyzer.model;

import java.util.Objects;

/**
 * Represents a method call relationship
 */
public class MethodCall {
    private String id;
    private String sourceMethodId;
    private String targetMethodId;
    private String targetQualifiedMethod;  // For unresolved external calls
    private CallType callType;
    private int sourceLineNumber;
    private String callerExpression;
    private boolean crossService;
    private String targetService;

    public MethodCall() {
    }

    public MethodCall(String id, String sourceMethodId, String targetMethodId, CallType callType) {
        this.id = id;
        this.sourceMethodId = sourceMethodId;
        this.targetMethodId = targetMethodId;
        this.callType = callType;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceMethodId() {
        return sourceMethodId;
    }

    public void setSourceMethodId(String sourceMethodId) {
        this.sourceMethodId = sourceMethodId;
    }

    public String getTargetMethodId() {
        return targetMethodId;
    }

    public void setTargetMethodId(String targetMethodId) {
        this.targetMethodId = targetMethodId;
    }

    public String getTargetQualifiedMethod() {
        return targetQualifiedMethod;
    }

    public void setTargetQualifiedMethod(String targetQualifiedMethod) {
        this.targetQualifiedMethod = targetQualifiedMethod;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public int getSourceLineNumber() {
        return sourceLineNumber;
    }

    public void setSourceLineNumber(int sourceLineNumber) {
        this.sourceLineNumber = sourceLineNumber;
    }

    public String getCallerExpression() {
        return callerExpression;
    }

    public void setCallerExpression(String callerExpression) {
        this.callerExpression = callerExpression;
    }

    public boolean isCrossService() {
        return crossService;
    }

    public void setCrossService(boolean crossService) {
        this.crossService = crossService;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodCall that = (MethodCall) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MethodCall{" +
                "id='" + id + '\'' +
                ", callType=" + callType +
                ", crossService=" + crossService +
                '}';
    }
}
