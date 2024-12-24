package com.gantenx.utils.calculator;

import com.gantenx.constant.Symbol;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.constant.Constants.FEE;
import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;

@Slf4j
public class AssetCalculator {

    public static Map<Long, Double> calculateAssetMap(Map<Symbol, Map<Long, Kline>> klineMap,
                                                      List<Long> timestampList,
                                                      List<Order> orders,
                                                      double init) {
        // 参数验证
        if (CollectionUtils.isEmpty(timestampList) || klineMap == null || init < 0) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        Map<Long, Double> assetMap = new TreeMap<>();
        double currentBalance = init;
        Map<Symbol, Double> currentPosition = new HashMap<>();

        // 设置初始资产
        Long startTimestamp = timestampList.get(0);
        assetMap.put(startTimestamp, currentBalance);

        // 按时间戳组织订单
        Map<Long, List<Order>> orderMap = CollectionUtils.toListMap(orders);

        // 遍历每个交易日
        for (Long timestamp : timestampList) {
            // 处理当天的订单
            List<Order> orderList = orderMap.get(timestamp);
            if (!CollectionUtils.isEmpty(orderList)) {
                for (Order order : orderList) {
                    Symbol symbol = order.getSymbol();
                    double price = order.getPrice();
                    double quantity = order.getQuantity();
                    double fee = price * quantity * FEE;

                    if (order.getType() == BUY) {
                        currentBalance -= (price * quantity + fee);
                        currentPosition.merge(symbol, quantity, Double::sum);
                    } else if (order.getType() == SELL) {
                        currentBalance += (price * quantity - fee);
                        currentPosition.merge(symbol, -quantity, Double::sum);
                    }
                }
            }

            // 计算当天的持仓市值
            double assetOfPosition = calculatePositionAsset(currentPosition, klineMap, timestamp);
            // 更新当天的总资产
            assetMap.put(timestamp, assetOfPosition + currentBalance);
        }
        return assetMap;
    }

    // 抽取持仓市值计算逻辑
    private static double calculatePositionAsset(Map<Symbol, Double> positions,
                                                 Map<Symbol, Map<Long, Kline>> klineMap,
                                                 long timestamp) {
        return positions.entrySet().stream().mapToDouble(entry -> {
            Symbol symbol = entry.getKey();
            double quantity = entry.getValue();
            Kline kline = CollectionUtils.get(klineMap, symbol, timestamp);
            if (kline == null) {
                throw new IllegalStateException("No kline data found for " + symbol + " at " + timestamp);
            }
            return kline.getClose() * quantity;
        }).sum();
    }
}
