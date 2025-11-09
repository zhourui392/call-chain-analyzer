package com.example.user.model;

public class User {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String level;
    private String address;

    public User() {
    }

    public User(Long id, String name, String email, String phone, String level) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.level = level;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
