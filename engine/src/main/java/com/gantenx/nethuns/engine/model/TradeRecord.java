package com.gantenx.nethuns.engine.model;

import com.gantenx.nethuns.commons.annotation.ExcelColumn;

import java.util.List;

public class TradeRecord {
    private double balance;
    private double initialBalance;
    private double feeCount;
    @ExcelColumn(need = false)
    private List<Order> orders;
    @ExcelColumn(need = false)
    private List<Trade> records;

    public List<Trade> getRecords() {
        return records;
    }

    public void setRecords(List<Trade> records) {
        this.records = records;
    }

    public TradeRecord() {
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
