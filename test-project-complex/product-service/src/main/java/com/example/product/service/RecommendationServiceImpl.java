package com.example.product.service;

import com.example.product.api.RecommendationService;
import com.example.product.model.Inventory;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@DubboService(version = "1.0.0", group = "ecommerce")
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public List<Product> recommend(Long userId, List<String> orderHistory) {
        // 简单推荐逻辑：基于订单历史中的商品分类推荐同类商品
        if (orderHistory == null || orderHistory.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取最常购买的分类（这里简化为第一个分类）
        String category = orderHistory.get(0);

        // 查找该分类的商品
        List<Product> products = productRepository.findByCategory(category);

        // 过滤有库存的商品
        List<String> productIds = new ArrayList<>();
        for (Product product : products) {
            productIds.add(product.getId());
        }

        List<Inventory> inStockInventories = inventoryService.filterInStock(productIds);

        // 返回有库存的商品
        List<Product> recommendations = new ArrayList<>();
        for (Inventory inventory : inStockInventories) {
            Product product = productRepository.findById(inventory.getProductId());
            if (product != null) {
                recommendations.add(product);
            }
        }

        return recommendations;
    }
}
