package com.gantenx.calculator;

import com.gantenx.model.Order;
import com.gantenx.model.ProfitResult;

import java.util.*;

public class OrderCalculator {

    public static Map<String, ProfitResult> calculateProfitAndHoldingDays(List<Order> orderList) {
        Map<String, ProfitResult> results = new HashMap<>();
        Map<String, Stack<Order>> buyOrdersMap = new HashMap<>(); // 用于存储未卖出的买单

        for (Order order : orderList) {
            String symbol = order.getSymbol();

            if (!results.containsKey(symbol)) {
                results.put(symbol, new ProfitResult());
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
                    ProfitResult profitResult = results.get(symbol);
                    profitResult.addProfit(profit);
                    profitResult.addHoldingDays(holdingDays);
                }
            }
        }

        return results;
    }
}
