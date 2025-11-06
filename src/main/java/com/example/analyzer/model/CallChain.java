package com.example.analyzer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a complete call chain from an entry point through multiple method calls
 */
public class CallChain {
    private String id;
    private CallChainNode entryPoint;
    private List<CallChainNode> chain;
    private int maxDepth;
    private List<String> involvedServices;
    private boolean crossService;

    public CallChain() {
        this.chain = new ArrayList<>();
        this.involvedServices = new ArrayList<>();
    }

    public CallChain(String id) {
        this();
        this.id = id;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CallChainNode getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(CallChainNode entryPoint) {
        this.entryPoint = entryPoint;
    }

    public List<CallChainNode> getChain() {
        return chain;
    }

    public void setChain(List<CallChainNode> chain) {
        this.chain = chain;
    }

    public void addNode(CallChainNode node) {
        this.chain.add(node);
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public List<String> getInvolvedServices() {
        return involvedServices;
    }

    public void setInvolvedServices(List<String> involvedServices) {
        this.involvedServices = involvedServices;
    }

    public void addInvolvedService(String serviceId) {
        if (!this.involvedServices.contains(serviceId)) {
            this.involvedServices.add(serviceId);
        }
    }

    public boolean isCrossService() {
        return crossService;
    }

    public void setCrossService(boolean crossService) {
        this.crossService = crossService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallChain callChain = (CallChain) o;
        return Objects.equals(id, callChain.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CallChain{" +
                "id='" + id + '\'' +
                ", maxDepth=" + maxDepth +
                ", crossService=" + crossService +
                ", nodes=" + chain.size() +
                '}';
    }

    /**
     * A node in the call chain
     */
    public static class CallChainNode {
        private int level;
        private String methodId;
        private String classId;
        private String serviceId;
        private CallType callType;
        private String httpEndpoint;  // For entry points

        public CallChainNode() {
        }

        public CallChainNode(int level, String methodId, String classId, String serviceId) {
            this.level = level;
            this.methodId = methodId;
            this.classId = classId;
            this.serviceId = serviceId;
        }

        // Getters and Setters
        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getMethodId() {
            return methodId;
        }

        public void setMethodId(String methodId) {
            this.methodId = methodId;
        }

        public String getClassId() {
            return classId;
        }

        public void setClassId(String classId) {
            this.classId = classId;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public CallType getCallType() {
            return callType;
        }

        public void setCallType(CallType callType) {
            this.callType = callType;
        }

        public String getHttpEndpoint() {
            return httpEndpoint;
        }

        public void setHttpEndpoint(String httpEndpoint) {
            this.httpEndpoint = httpEndpoint;
        }

        @Override
        public String toString() {
            return "CallChainNode{" +
                    "level=" + level +
                    ", methodId='" + methodId + '\'' +
                    ", serviceId='" + serviceId + '\'' +
                    '}';
        }
    }
}
