package com.example.analyzer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete analysis result containing all services, classes, methods, and call relationships
 */
public class AnalysisResult {
    private AnalysisMetadata metadata;
    private List<ServiceInfo> services;
    private List<ClassInfo> classes;
    private List<MethodInfo> methods;
    private List<MethodCall> methodCalls;
    private List<CallChain> callChains;

    // Internal indexes for fast lookup
    private transient Map<String, ClassInfo> classIndex;
    private transient Map<String, MethodInfo> methodIndex;
    private transient Map<String, ServiceInfo> serviceIndex;

    public AnalysisResult() {
        this.metadata = new AnalysisMetadata();
        this.services = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.methodCalls = new ArrayList<>();
        this.callChains = new ArrayList<>();
        this.classIndex = new HashMap<>();
        this.methodIndex = new HashMap<>();
        this.serviceIndex = new HashMap<>();
    }

    // Getters and Setters
    public AnalysisMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AnalysisMetadata metadata) {
        this.metadata = metadata;
    }

    public List<ServiceInfo> getServices() {
        return services;
    }

    public void setServices(List<ServiceInfo> services) {
        this.services = services;
        rebuildServiceIndex();
    }

    public void addService(ServiceInfo service) {
        this.services.add(service);
        this.serviceIndex.put(service.getId(), service);
    }

    public List<ClassInfo> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassInfo> classes) {
        this.classes = classes;
        rebuildClassIndex();
    }

    public void addClass(ClassInfo classInfo) {
        this.classes.add(classInfo);
        this.classIndex.put(classInfo.getId(), classInfo);
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
        rebuildMethodIndex();
    }

    public void addMethod(MethodInfo method) {
        this.methods.add(method);
        this.methodIndex.put(method.getId(), method);
    }

    public List<MethodCall> getMethodCalls() {
        return methodCalls;
    }

    public void setMethodCalls(List<MethodCall> methodCalls) {
        this.methodCalls = methodCalls;
    }

    public void addMethodCall(MethodCall methodCall) {
        this.methodCalls.add(methodCall);
    }

    public List<CallChain> getCallChains() {
        return callChains;
    }

    public void setCallChains(List<CallChain> callChains) {
        this.callChains = callChains;
    }

    public void addCallChain(CallChain callChain) {
        this.callChains.add(callChain);
    }

    // Lookup methods
    public ClassInfo getClassById(String id) {
        return classIndex.get(id);
    }

    public MethodInfo getMethodById(String id) {
        return methodIndex.get(id);
    }

    public ServiceInfo getServiceById(String id) {
        return serviceIndex.get(id);
    }

    // Index rebuilding methods
    private void rebuildClassIndex() {
        this.classIndex.clear();
        for (ClassInfo classInfo : classes) {
            this.classIndex.put(classInfo.getId(), classInfo);
        }
    }

    private void rebuildMethodIndex() {
        this.methodIndex.clear();
        for (MethodInfo method : methods) {
            this.methodIndex.put(method.getId(), method);
        }
    }

    private void rebuildServiceIndex() {
        this.serviceIndex.clear();
        for (ServiceInfo service : services) {
            this.serviceIndex.put(service.getId(), service);
        }
    }

    public void buildIndexes() {
        rebuildServiceIndex();
        rebuildClassIndex();
        rebuildMethodIndex();
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "services=" + services.size() +
                ", classes=" + classes.size() +
                ", methods=" + methods.size() +
                ", methodCalls=" + methodCalls.size() +
                ", callChains=" + callChains.size() +
                '}';
    }

    /**
     * Metadata about the analysis
     */
    public static class AnalysisMetadata {
        private LocalDateTime analysisTime;
        private String projectName;
        private int totalServices;
        private int totalClasses;
        private int totalMethods;

        public AnalysisMetadata() {
            this.analysisTime = LocalDateTime.now();
        }

        // Getters and Setters
        public LocalDateTime getAnalysisTime() {
            return analysisTime;
        }

        public void setAnalysisTime(LocalDateTime analysisTime) {
            this.analysisTime = analysisTime;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public int getTotalServices() {
            return totalServices;
        }

        public void setTotalServices(int totalServices) {
            this.totalServices = totalServices;
        }

        public int getTotalClasses() {
            return totalClasses;
        }

        public void setTotalClasses(int totalClasses) {
            this.totalClasses = totalClasses;
        }

        public int getTotalMethods() {
            return totalMethods;
        }

        public void setTotalMethods(int totalMethods) {
            this.totalMethods = totalMethods;
        }
    }
}
