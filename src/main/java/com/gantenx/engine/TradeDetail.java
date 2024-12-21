package com.gantenx.engine;

import com.gantenx.annotation.ExcelColumn;

import java.util.List;

public class TradeDetail {
    private double balance;
    private double initialBalance;
    private double feeCount;
    @ExcelColumn(need = false)
    private List<Order> orders;
    @ExcelColumn(need = false)
    private List<TradeRecord> records;

    public List<TradeRecord> getRecords() {
        return records;
    }

    public void setRecords(List<TradeRecord> records) {
        this.records = records;
    }

    public TradeDetail() {
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public double getFeeCount() {
        return feeCount;
    }

    public void setFeeCount(double feeCount) {
        this.feeCount = feeCount;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
