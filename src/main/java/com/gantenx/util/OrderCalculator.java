package com.gantenx.util;

import com.gantenx.model.Order;

import java.util.*;

public class OrderCalculator {

    public static class Result {
        private double profit;
        private long totalHoldingDays;

        // Constructor
        public Result() {
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

    public static Map<String, Result> calculateProfitAndHoldingDays(List<Order> orderList) {
        Map<String, Result> results = new HashMap<>();
        Map<String, Stack<Order>> buyOrdersMap = new HashMap<>(); // 用于存储未卖出的买单

        for (Order order : orderList) {
            String symbol = order.getSymbol();

            if (!results.containsKey(symbol)) {
                results.put(symbol, new Result());
            }

            // 处理买入操作
            if (order.getType().equals("buy")) {
                buyOrdersMap.computeIfAbsent(symbol, k -> new Stack<>()).push(order);
            }
            // 处理卖出操作
            else if (order.getType().equals("sell")) {
                Stack<Order> buyOrders = buyOrdersMap.get(symbol);
                if (!buyOrders.isEmpty()) {
                    // 计算收益
                    Order buyOrder = buyOrders.pop();
                    double profit = (order.getPrice() - buyOrder.getPrice()) * order.getQuantity();

                    // 计算持有天数
                    long holdingDays = (order.getTimestamp() - buyOrder.getTimestamp()) / (1000 * 60 * 60 * 24); // 转换为天数

                    // 更新结果
                    Result result = results.get(symbol);
                    result.addProfit(profit);
                    result.addHoldingDays(holdingDays);
                }
            }
        }

        return results;
    }
}
