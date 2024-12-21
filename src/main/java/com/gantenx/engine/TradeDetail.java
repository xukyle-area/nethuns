package com.gantenx.engine;

import com.gantenx.annotation.ExcelColumn;

import java.util.List;

public class TradeDetail<T> {
    private double balance;
    private double initialBalance;
    private double feeCount;
    @ExcelColumn(need = false)
    private List<Order<T>> orders;
    @ExcelColumn(need = false)
    private List<TradeRecord<T>> records;

    public List<TradeRecord<T>> getRecords() {
        return records;
    }

    public void setRecords(List<TradeRecord<T>> records) {
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

    public List<Order<T>> getOrders() {
        return orders;
    }

    public void setOrders(List<Order<T>> orders) {
        this.orders = orders;
    }
}
