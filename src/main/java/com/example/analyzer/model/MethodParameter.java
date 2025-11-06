package com.example.analyzer.model;

import java.util.Objects;

/**
 * Represents a method parameter
 */
public class MethodParameter {
    private String name;
    private String type;
    private int index;

    public MethodParameter() {
    }

    public MethodParameter(String name, String type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodParameter that = (MethodParameter) o;
        return index == that.index && Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, index);
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
