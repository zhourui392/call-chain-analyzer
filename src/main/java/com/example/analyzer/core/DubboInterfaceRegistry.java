package com.example.analyzer.core;

import com.example.analyzer.model.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Dubbo interface to implementation mapping registry
 * Maps Dubbo service interfaces to their implementations across services
 */
public class DubboInterfaceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DubboInterfaceRegistry.class);

    /**
     * Map: Interface qualified name -> List of implementations
     */
    private final Map<String, List<DubboServiceImpl>> interfaceToImpls = new HashMap<>();

    /**
     * Represents a Dubbo service implementation
     */
    public static class DubboServiceImpl {
        private String serviceId;       // Service that provides this implementation
        private String implClassId;     // Implementation class ID
        private String implClassName;   // Implementation class name
        private String interfaceName;   // Interface name
        private String version;         // Dubbo version (optional)
        private String group;           // Dubbo group (optional)

        public DubboServiceImpl(String serviceId, String implClassId, String implClassName, String interfaceName) {
            this.serviceId = serviceId;
            this.implClassId = implClassId;
            this.implClassName = implClassName;
            this.interfaceName = interfaceName;
            this.version = "";
            this.group = "";
        }

        // Getters and Setters
        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getImplClassId() {
            return implClassId;
        }

        public void setImplClassId(String implClassId) {
            this.implClassId = implClassId;
        }

        public String getImplClassName() {
            return implClassName;
        }

        public void setImplClassName(String implClassName) {
            this.implClassName = implClassName;
        }

        public String getInterfaceName() {
            return interfaceName;
        }

        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        @Override
        public String toString() {
            return "DubboServiceImpl{" +
                    "serviceId='" + serviceId + '\'' +
                    ", implClassName='" + implClassName + '\'' +
                    ", interfaceName='" + interfaceName + '\'' +
                    '}';
        }
    }

    /**
     * Register a Dubbo service implementation
     */
    public void registerDubboService(String interfaceName, DubboServiceImpl impl) {
        interfaceToImpls.computeIfAbsent(interfaceName, k -> new ArrayList<>()).add(impl);
        logger.debug("Registered Dubbo service: {} -> {}", interfaceName, impl.getImplClassName());
    }

    /**
     * Find implementation by interface name
     * Returns the first matching implementation (simplified version)
     */
    public DubboServiceImpl resolve(String interfaceName) {
        List<DubboServiceImpl> impls = interfaceToImpls.get(interfaceName);
        if (impls == null || impls.isEmpty()) {
            logger.debug("No implementation found for interface: {}", interfaceName);
            return null;
        }

        if (impls.size() > 1) {
            logger.warn("Multiple implementations found for interface: {}, using first one", interfaceName);
        }

        return impls.get(0);
    }

    /**
     * Find implementation by interface name with version and group matching
     */
    public DubboServiceImpl resolve(String interfaceName, String version, String group) {
        List<DubboServiceImpl> impls = interfaceToImpls.get(interfaceName);
        if (impls == null || impls.isEmpty()) {
            return null;
        }

        // Try to match version and group
        for (DubboServiceImpl impl : impls) {
            boolean versionMatch = version == null || version.isEmpty() || version.equals(impl.getVersion());
            boolean groupMatch = group == null || group.isEmpty() || group.equals(impl.getGroup());
            if (versionMatch && groupMatch) {
                return impl;
            }
        }

        // Fallback to first implementation
        logger.debug("No exact version/group match for {}, using first implementation", interfaceName);
        return impls.get(0);
    }

    /**
     * Get all registered interfaces
     */
    public Set<String> getAllInterfaces() {
        return interfaceToImpls.keySet();
    }

    /**
     * Get all implementations for an interface
     */
    public List<DubboServiceImpl> getAllImplementations(String interfaceName) {
        return interfaceToImpls.getOrDefault(interfaceName, Collections.emptyList());
    }

    /**
     * Build registry from analysis result
     */
    public void buildFromClasses(List<ClassInfo> classes) {
        for (ClassInfo classInfo : classes) {
            // Only process @DubboService classes
            if (classInfo.getType() != null &&
                classInfo.getType().toString().equals("DUBBO_SERVICE")) {

                // Extract implemented interfaces from class
                // In this simplified version, we try to infer from class name
                // Format: OrderServiceImpl implements OrderService
                String className = classInfo.getClassName();

                // Try to find interface by removing "Impl" suffix
                String possibleInterfaceName = null;
                if (className.endsWith("Impl")) {
                    possibleInterfaceName = className.substring(0, className.length() - 4);
                }

                if (possibleInterfaceName != null) {
                    // Build full qualified interface name
                    String packageName = classInfo.getPackageName();
                    // Common pattern: impl classes are in .impl package, interfaces in parent package
                    if (packageName.endsWith(".impl")) {
                        packageName = packageName.substring(0, packageName.length() - 5);
                    }
                    String interfaceQualifiedName = packageName + "." + possibleInterfaceName;

                    DubboServiceImpl impl = new DubboServiceImpl(
                        classInfo.getServiceId(),
                        classInfo.getId(),
                        classInfo.getQualifiedName(),
                        interfaceQualifiedName
                    );

                    registerDubboService(interfaceQualifiedName, impl);

                    // Also register with simple interface name for easier lookup
                    registerDubboService(possibleInterfaceName, impl);
                }
            }
        }

        logger.info("Built Dubbo interface registry with {} interfaces", interfaceToImpls.size());
    }

    /**
     * Clear all registrations
     */
    public void clear() {
        interfaceToImpls.clear();
    }

    /**
     * Get registry statistics
     */
    public String getStatistics() {
        int totalInterfaces = interfaceToImpls.size();
        int totalImpls = interfaceToImpls.values().stream()
                .mapToInt(List::size)
                .sum();
        return String.format("Interfaces: %d, Implementations: %d", totalInterfaces, totalImpls);
    }
}
