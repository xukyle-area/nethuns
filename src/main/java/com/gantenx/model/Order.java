package com.gantenx.model;

public class Order {
    private final String type;       // "buy" or "sell"
    private final double price;      // 价格
    private final double quantity;   // 数量
    private final long timestamp;    // 时间戳

    public Order(String type, double price, double quantity, long timestamp) {
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Order{" +
                "type='" + type + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                '}';
    }
}
