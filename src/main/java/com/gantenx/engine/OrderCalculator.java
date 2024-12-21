package com.gantenx.engine;

import com.gantenx.model.Profit;
import com.gantenx.constant.Symbol;

import java.util.*;

import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;

public class OrderCalculator {

    public static List<Profit> calculateProfitAndHoldingDays(List<Order> orderList) {
        Map<Symbol, Profit> results = new HashMap<>();
        Map<Symbol, Stack<Order>> buyOrdersMap = new HashMap<>(); // 用于存储未卖出的买单

        for (Order order : orderList) {
            Symbol symbol = order.getSymbol();

            if (!results.containsKey(symbol)) {
                Profit profit = new Profit();
                profit.setSymbol(symbol);
                results.put(symbol, profit);
            }

            if (order.getType().equals(BUY)) {
                buyOrdersMap.computeIfAbsent(symbol, k -> new Stack<>()).push(order);
            } else if (order.getType().equals(SELL)) {
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
        return new ArrayList<>(results.values());
    }
}
