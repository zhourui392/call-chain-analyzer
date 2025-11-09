package com.example.product.repository;

import com.example.product.model.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository {
    private final Map<String, Product> products = new HashMap<>();

    public ProductRepository() {
        // 初始化测试数据
        products.put("P001", new Product("P001", "iPhone 15", "Electronics", 5999.0));
        products.put("P002", new Product("P002", "MacBook Pro", "Electronics", 12999.0));
        products.put("P003", new Product("P003", "AirPods Pro", "Electronics", 1999.0));
        products.put("P004", new Product("P004", "Nike Running Shoes", "Sports", 699.0));
        products.put("P005", new Product("P005", "Adidas T-Shirt", "Sports", 199.0));
    }

    public Product findById(String id) {
        return products.get(id);
    }

    public List<Product> findByCategory(String category) {
        List<Product> result = new ArrayList<>();
        for (Product product : products.values()) {
            if (product.getCategory().equals(category)) {
                result.add(product);
            }
        }
        return result;
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
}
