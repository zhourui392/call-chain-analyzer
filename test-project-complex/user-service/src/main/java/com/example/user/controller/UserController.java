package com.example.user.controller;

import com.example.user.model.User;
import com.example.user.service.UserFacadeService;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserFacadeService userFacadeService;

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserWithOrders(id);
    }

    @PostMapping("/{userId}/orders")
    public String createOrder(
            @PathVariable Long userId,
            @RequestParam String productId,
            @RequestParam Integer quantity,
            @RequestParam String address) {
        return userFacadeService.placeOrder(userId, productId, quantity, address);
    }

    @GetMapping("/{id}/recommendations")
    public List<com.example.product.model.Product> getRecommendations(@PathVariable Long id) {
        return userFacadeService.getRecommendations(id);
    }
}
