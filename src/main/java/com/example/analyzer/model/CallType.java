package com.example.analyzer.model;

/**
 * Types of method calls
 */
public enum CallType {
    INTERNAL_METHOD_CALL,   // Method call within the same service
    RPC_METHOD_CALL,        // Dubbo RPC call to another service
    HTTP_METHOD_CALL,       // HTTP call to another service
    STATIC_METHOD_CALL,     // Static method call
    CONSTRUCTOR_CALL        // Constructor invocation
}
