package com.example.analyzer.model;

/**
 * Classification of class types based on Spring annotations
 */
public enum ClassType {
    CONTROLLER,      // @Controller, @RestController
    SERVICE,         // @Service
    REPOSITORY,      // @Repository
    COMPONENT,       // @Component
    CONFIGURATION,   // @Configuration
    INTERFACE,       // Interface definition
    PLAIN_CLASS,     // Regular class without Spring annotations
    DUBBO_SERVICE,   // @DubboService
    UNKNOWN
}
