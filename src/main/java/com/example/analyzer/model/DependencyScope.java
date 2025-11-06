package com.example.analyzer.model;

/**
 * Scope of dependency - internal or cross-service
 */
public enum DependencyScope {
    INTERNAL,   // Within the same service
    RPC,        // Cross-service via Dubbo
    HTTP,       // Cross-service via HTTP
    MQ          // Cross-service via Message Queue
}
