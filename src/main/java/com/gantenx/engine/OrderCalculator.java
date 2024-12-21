package com.gantenx.engine;

import com.gantenx.calculator.Profit;

import java.util.*;

import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;

public class OrderCalculator {

    public static <T> List<Profit<T>> calculateProfitAndHoldingDays(List<Order<T>> orderList) {
        Map<T, Profit<T>> results = new HashMap<>();
        Map<T, Stack<Order<T>>> buyOrdersMap = new HashMap<>(); // 用于存储未卖出的买单

        for (Order<T> order : orderList) {
            T symbol = order.getSymbol();

            if (!results.containsKey(symbol)) {
                Profit<T> profit = new Profit<>();
                profit.setSymbol(symbol);
                results.put(symbol, profit);
            }

            // 处理买入操作
            if (order.getType().equals(BUY)) {
                buyOrdersMap.computeIfAbsent(symbol, k -> new Stack<>()).push(order);
            }
            // 处理卖出操作
            else if (order.getType().equals(SELL)) {
                Stack<Order<T>> buyOrders = buyOrdersMap.get(symbol);
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
        return new ArrayList<>(results.values());
    }
}
