package com.example.analyzer.model;

/**
 * Types of dependency injection
 */
public enum InjectionType {
    AUTOWIRED,         // @Autowired
    RESOURCE,          // @Resource
    INJECT,            // @Inject
    CONSTRUCTOR,       // Constructor injection
    SETTER,            // Setter injection
    DUBBO_REFERENCE,   // @DubboReference
    NONE               // No injection (direct instantiation)
}
