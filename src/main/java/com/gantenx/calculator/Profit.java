package com.gantenx.calculator;

public class Profit {
    private double profit;
    private long totalHoldingDays;

    // Constructor
    public Profit() {
        this.profit = 0;
        this.totalHoldingDays = 0;
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

