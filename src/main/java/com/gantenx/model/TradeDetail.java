package com.gantenx.model;

import com.gantenx.annotation.ExcelColumn;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public class TradeDetail {
    private double balance;
    private double initialBalance;
    private double feeCount;
    @ExcelColumn(need = false)
    private Map<String, Pair<Double, Double>> positionMap;
    @ExcelColumn(need = false)
    private List<Order> orders;

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

    public Map<String, Pair<Double, Double>> getPositionMap() {
        return positionMap;
    }

    public void setPositionMap(Map<String, Pair<Double, Double>> positionMap) {
        this.positionMap = positionMap;
    }
}
