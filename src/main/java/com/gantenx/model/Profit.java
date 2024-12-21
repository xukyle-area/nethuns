package com.gantenx.model;

import com.gantenx.constant.Symbol;

public class Profit {
    private double profit;
    private long totalHoldingDays;
    private Symbol symbol;

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

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
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

