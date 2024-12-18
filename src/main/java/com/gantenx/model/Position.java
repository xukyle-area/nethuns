package com.gantenx.model;

public class Position {
    private double averagePrice;
    private double quantity;

    public Position(double averagePrice, double quantity) {
        this.averagePrice = averagePrice;
        this.quantity = quantity;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void addPosition(double price, double quantity) {
        double totalCost = this.averagePrice * this.quantity + price * quantity;
        this.quantity += quantity;
        this.averagePrice = totalCost / this.quantity;
    }

    public void reducePosition(double quantity) {
        this.quantity -= quantity;
        if (this.quantity < 1e-6) {
            this.quantity = 0; // Avoid precision errors
            this.averagePrice = 0;
        }
    }
}