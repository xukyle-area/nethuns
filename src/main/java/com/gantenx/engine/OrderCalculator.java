package com.gantenx.engine;

import com.gantenx.calculator.Profit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;

public class OrderCalculator {

    public static Map<String, Profit> calculateProfitAndHoldingDays(List<Order> orderList) {
        Map<String, Profit> results = new HashMap<>();
        Map<String, Stack<Order>> buyOrdersMap = new HashMap<>(); // 用于存储未卖出的买单

        for (Order order : orderList) {
            String symbol = order.getSymbol();

            if (!results.containsKey(symbol)) {
                results.put(symbol, new Profit());
            }

            // 处理买入操作
            if (order.getType().equals(BUY)) {
                buyOrdersMap.computeIfAbsent(symbol, k -> new Stack<>()).push(order);
            }
            // 处理卖出操作
            else if (order.getType().equals(SELL)) {
                Stack<Order> buyOrders = buyOrdersMap.get(symbol);
                if (!buyOrders.isEmpty()) {
                    // 计算收益
                    Order buyOrder = buyOrders.pop();
                    double profit = (order.getPrice() - buyOrder.getPrice()) * order.getQuantity();

                    // 计算持有天数
                    long holdingDays = (order.getTimestamp() - buyOrder.getTimestamp()) / (1000 * 60 * 60 * 24); // 转换为天数

                    // 更新结果
                    Profit profitResult = results.get(symbol);
                    profitResult.addProfit(profit);
                    profitResult.addHoldingDays(holdingDays);
                }
            }
        }

        return results;
    }
}
