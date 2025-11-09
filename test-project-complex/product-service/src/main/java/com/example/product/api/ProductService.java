package com.example.product.api;

import com.example.product.model.Product;

/**
 * 商品服务 Dubbo 接口
 */
public interface ProductService {

    /**
     * 检查库存是否充足
     */
    boolean checkInventory(String productId, Integer quantity);

    /**
     * 获取商品信息
     */
    Product getProductInfo(String productId);

    /**
     * 扣减库存
     */
    boolean decreaseInventory(String productId, Integer quantity);
}
