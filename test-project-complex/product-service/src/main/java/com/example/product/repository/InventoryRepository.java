package com.example.product.repository;

import com.example.product.model.Inventory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InventoryRepository {
    private final Map<String, Inventory> inventories = new HashMap<>();

    public InventoryRepository() {
        // 初始化库存数据
        inventories.put("P001", new Inventory("P001", 100, 10));
        inventories.put("P002", new Inventory("P002", 50, 5));
        inventories.put("P003", new Inventory("P003", 200, 20));
        inventories.put("P004", new Inventory("P004", 150, 15));
        inventories.put("P005", new Inventory("P005", 300, 30));
    }

    public Inventory findByProductId(String productId) {
        return inventories.get(productId);
    }

    public List<Inventory> findInStock(List<String> productIds) {
        List<Inventory> result = new ArrayList<>();
        for (String productId : productIds) {
            Inventory inventory = inventories.get(productId);
            if (inventory != null && inventory.getAvailable() > 0) {
                result.add(inventory);
            }
        }
        return result;
    }

    public Inventory update(String productId, Integer newStock) {
        Inventory inventory = inventories.get(productId);
        if (inventory != null) {
            inventory.setStock(newStock);
        }
        return inventory;
    }
}
