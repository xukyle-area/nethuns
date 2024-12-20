package com.gantenx.engine;

public class Position {
    // 买入的时候的订单id
    private long orderId;
    // 买入价格
    private double price;
    // 持仓数量
    private double quantity;
    // 买入时间
    private long timestamp;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Position() {
    }

    public Position(long orderId, double price, double quantity, long timestamp) {
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void addPosition(double price, double quantity) {
        double totalCost = this.price * this.quantity + price * quantity;
        this.quantity += quantity;
        this.price = totalCost / this.quantity;
    }

    public void reducePosition(double quantity) {
        this.quantity -= quantity;
        if (this.quantity < 1e-6) {
            this.quantity = 0; // Avoid precision errors
            this.price = 0;
        }
    }
}