package com.example.product.service;

import com.example.product.model.Inventory;
import com.example.product.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public boolean checkStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory == null) {
            return false;
        }
        return inventory.getAvailable() >= quantity;
    }

    public Inventory decrease(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory != null && inventory.getAvailable() >= quantity) {
            Integer newStock = inventory.getStock() - quantity;
            return inventoryRepository.update(productId, newStock);
        }
        return null;
    }

    public List<Inventory> filterInStock(List<String> productIds) {
        return inventoryRepository.findInStock(productIds);
    }
}
