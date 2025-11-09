package com.example.product.api;

import com.example.product.model.Product;

import java.util.List;

/**
 * 商品推荐服务 Dubbo 接口
 */
public interface RecommendationService {

    /**
     * 根据用户订单历史推荐商品
     * @param userId 用户ID
     * @param orderHistory 订单历史（商品分类列表）
     * @return 推荐商品列表
     */
    List<Product> recommend(Long userId, List<String> orderHistory);
}
