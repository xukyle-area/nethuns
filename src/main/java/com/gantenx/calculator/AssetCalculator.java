package com.gantenx.calculator;

import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;
import static com.gantenx.utils.DateUtils.MS_OF_ONE_DAY;

@Slf4j
public class AssetCalculator {

    // 用于存储每个时间戳对应的资产
    public static <T> Map<Long, Double> calculateAssetMap(Map<Long, Kline> klineMap,
                                                          List<Order<T>> orders,
                                                          double initialBalance) {
        Map<Long, Double> assetMap = new TreeMap<>();
        double currentBalance = initialBalance;
        double currentPosition = 0.0;

        Long startTimestamp = CollectionUtils.getMinKey(klineMap);
        assetMap.put(startTimestamp, currentBalance);

        Map<Long, Order<T>> orderMap = CollectionUtils.toTimeMap(orders);

        Long endTimestamp = CollectionUtils.getMaxKey(klineMap);
        for (long i = startTimestamp; i <= endTimestamp; i += MS_OF_ONE_DAY) {
            Order<T> order = orderMap.get(i);
            Kline kline = klineMap.get(i);
            double price = kline.getClose();
            if (Objects.nonNull(order)) {
                double quantity = order.getQuantity();
                if (order.getType() == BUY) {
                    currentBalance -= price * quantity;
                    currentPosition += quantity;
                } else if (order.getType() == SELL) {
                    currentBalance += price * quantity;
                    currentPosition -= quantity;
                }
            }
            double asset = currentBalance + currentPosition * price;
            assetMap.put(i, asset);
        }

        return assetMap;
    }
}
