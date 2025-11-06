package com.example.user.service;

import com.example.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public User findUserById(Long id) {
        // Simulate database lookup
        User user = new User();
        user.setId(id);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        return user;
    }

    public User createUser(User user) {
        // Simulate user creation
        user.setId(System.currentTimeMillis());
        return user;
    }

    public void deleteUser(Long id) {
        // Simulate deletion
        System.out.println("Deleting user: " + id);
    }
}
