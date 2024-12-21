package com.gantenx.calculator;

public class  Profit<T> {
    private double profit;
    private long totalHoldingDays;
    private T symbol;

    // Constructor
    public Profit() {
        this.profit = 0;
        this.totalHoldingDays = 0;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public void setTotalHoldingDays(long totalHoldingDays) {
        this.totalHoldingDays = totalHoldingDays;
    }

    public T getSymbol() {
        return symbol;
    }

    public void setSymbol(T symbol) {
        this.symbol = symbol;
    }

    // Add profit
    public void addProfit(double profit) {
        this.profit += profit;
    }

    // Add holding days
    public void addHoldingDays(long days) {
        this.totalHoldingDays += days;
    }

    // Getters
    public double getProfit() {
        return profit;
    }

    public long getTotalHoldingDays() {
        return totalHoldingDays;
    }
}

