package com.example.product.service;

import com.example.product.api.ProductService;
import com.example.product.model.Inventory;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService(version = "1.0.0", group = "ecommerce")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public boolean checkInventory(String productId, Integer quantity) {
        return inventoryService.checkStock(productId, quantity);
    }

    @Override
    public Product getProductInfo(String productId) {
        return productRepository.findById(productId);
    }

    @Override
    public boolean decreaseInventory(String productId, Integer quantity) {
        Inventory result = inventoryService.decrease(productId, quantity);
        return result != null;
    }
}
