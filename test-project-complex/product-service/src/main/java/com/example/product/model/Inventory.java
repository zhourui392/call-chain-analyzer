package com.example.product.model;

public class Inventory {
    private String productId;
    private Integer stock;
    private Integer reserved;
    private Integer available;

    public Inventory() {
    }

    public Inventory(String productId, Integer stock, Integer reserved) {
        this.productId = productId;
        this.stock = stock;
        this.reserved = reserved;
        this.available = stock - reserved;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
        this.available = stock - (reserved != null ? reserved : 0);
    }

    public Integer getReserved() {
        return reserved;
    }

    public void setReserved(Integer reserved) {
        this.reserved = reserved;
        this.available = (stock != null ? stock : 0) - reserved;
    }

    public Integer getAvailable() {
        return available;
    }
}
