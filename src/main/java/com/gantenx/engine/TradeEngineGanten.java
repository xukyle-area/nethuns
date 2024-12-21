package com.gantenx.engine;

import com.gantenx.engine.iface.TradeEngine;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;

@Slf4j
public class TradeEngineGanten<T> implements TradeEngine<T> {

    private static final double EPSILON = 1e-6;
    private final double initialBalance;
    private final double fee; // 交易手续费率
    private final Map<T, List<Position>> positions = new HashMap<>();
    private final List<Order<T>> orders = new ArrayList<>();
    private final List<TradeRecord<T>> records = new ArrayList<>();
    private double balance;
    private double feeCount;
    private long orderId = 0;
    private long recordId = 0;

    public TradeEngineGanten(double initialBalance, double fee) {
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.fee = fee;
        this.feeCount = 0.0;
    }

    @Override
    public boolean hasPosition() {
        Collection<Position> positionList = CollectionUtils.toCollection(positions.values());
        return positionList.stream().noneMatch(position -> position.getQuantity() > 0);
    }

    @Override
    public void sell(T symbol, double price, long proportion, long timestamp, String reason) {
        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            log.warn("{} - No position to sell for {}", DateUtils.getDate(timestamp), symbol);
            return;
        }

        double totalQuantity = positionList.stream().mapToDouble(Position::getQuantity).sum();
        double sellQuantity = totalQuantity * proportion / 100;

        if (sellQuantity <= 0) {
            log.warn("{} - Invalid sell quantity: sellQuantity={}, totalQuantity={}",
                     DateUtils.getDate(timestamp),
                     sellQuantity,
                     totalQuantity);
            return;
        }

        sell(symbol, price, sellQuantity, timestamp, reason);
    }

    @Override
    public void buy(T symbol, double price, long proportion, long timestamp, String reason) {
        double maxQuantity = (balance * proportion / 100) / (price * (1 + fee));  // 计算可以买入的最大数量
        if (maxQuantity <= 0) {
            log.warn("{} - Insufficient balance to buy {}: balance={}", DateUtils.getDate(timestamp), symbol, balance);
            return;
        }
        buy(symbol, price, maxQuantity, timestamp, reason);
    }

    private void buy(T symbol, double price, double quantity, long timestamp, String reason) {
        if (quantity <= 0 || price <= 0) {
            log.warn("{} - Invalid buy parameters: price={}, quantity={}",
                     DateUtils.getDate(timestamp),
                     price,
                     quantity);
            return;
        }

        double cost = calculateTotalCost(price, quantity);
        if (balance + EPSILON >= cost) {
            balance -= cost;

            List<Position> positionList = positions.getOrDefault(symbol, new ArrayList<>());
            long orderId = generateOrderId();
            positionList.add(new Position(orderId, price, quantity, timestamp)); // 保存买入记录
            positions.put(symbol, positionList);

            orders.add(new Order<>(orderId, symbol, BUY, price, quantity, timestamp, reason));

            log.info("{} - Bought {} {} at price {}, cost: {}, remaining balance: {}",
                     DateUtils.getDate(timestamp),
                     quantity,
                     symbol,
                     price,
                     cost,
                     balance);
        } else {
            log.warn("{} - Insufficient balance to buy {}: cost={}, balance={}",
                     DateUtils.getDate(timestamp),
                     symbol,
                     cost,
                     balance);
        }
    }

    private void sell(T symbol, double price, double quantity, long timestamp, String reason) {
        if (quantity <= 0 || price <= 0) {
            log.warn("{} - Invalid sell parameters: price={}, quantity={}",
                     DateUtils.getDate(timestamp),
                     price,
                     quantity);
            return;
        }

        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            log.warn("{} - No position to sell for {}", DateUtils.getDate(timestamp), symbol);
            return;
        }

        double remainingQuantity = quantity;
        Iterator<Position> iterator = positionList.iterator();
        double totalRevenue = 0.0;

        long orderId = generateOrderId();
        while (iterator.hasNext() && remainingQuantity > 0) {
            Position position = iterator.next();
            double sellQuantity = Math.min(position.getQuantity(), remainingQuantity);

            // 计算卖出收益
            double revenue = calculateTotalRevenue(price, sellQuantity);
            double profit = revenue - position.getPrice() * sellQuantity;

            totalRevenue += revenue;

            position.setQuantity(position.getQuantity() - sellQuantity);
            remainingQuantity -= sellQuantity;
            // 只在卖出的时候生成一条这个记录
            TradeRecord<T> record = new TradeRecord<>();
            record.setId(generateRecordId());
            record.setBuyOrderId(position.getOrderId());
            record.setHoldDays(DateUtils.getDaysBetween(position.getTimestamp(), timestamp));
            record.setBuyPrice(position.getPrice());
            record.setBuyTime(position.getTimestamp());
            record.setSellOrderId(orderId);
            record.setSellPrice(price);
            record.setSellTime(timestamp);
            record.setQuantity(quantity);
            record.setSymbol(symbol);
            record.setProfit(profit);
            record.setProfitRate(price / position.getPrice());
            records.add(record);

            // 如果仓位清空，移除该记录
            if (Math.abs(position.getQuantity()) < 1e-6) {
                iterator.remove();
            }
        }
        balance += totalRevenue;
        log.info("{} - Sold {} {} at price {}, revenue: {}, remaining balance: {}",
                 DateUtils.getDate(timestamp),
                 quantity,
                 symbol,
                 price,
                 totalRevenue,
                 balance);
        orders.add(new Order<>(orderId, symbol, SELL, price, quantity, timestamp, reason));
    }

    private double calculateTotalCost(double price, double quantity) {
        double cost = price * quantity;
        double curFee = cost * fee;
        feeCount += curFee;
        return cost + curFee;
    }

    private double calculateTotalRevenue(double price, double quantity) {
        double revenue = price * quantity;
        double curFee = revenue * fee;
        feeCount += curFee;
        return revenue - curFee;
    }

    @Override
    public double getQuantity(T symbol) {
        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            return 0d;
        }

        Optional<Double> optional = positionList.stream().map(Position::getQuantity).reduce(Double::sum);
        return optional.orElse(0d);
    }

    private long generateOrderId() {
        orderId++;
        return orderId;
    }

    private long generateRecordId() {
        recordId++;
        return recordId;
    }

    @Override
    public List<Position> getPositions(T symbol) {
        List<Position> list = positions.get(symbol);
        return new ArrayList<>(list);
    }

    @Override
    public double getBalance() {
        return this.balance;
    }

    @Override
    public double getPositionAsset(Map<T, Kline> priceMap) {
        double asset = 0;
        for (Map.Entry<T, Kline> entry : priceMap.entrySet()) {
            double quantity = this.getQuantity(entry.getKey());
            asset += quantity * entry.getValue().getClose();
        }
        return asset;
    }

    @Override
    public TradeDetail<T> exit(Map<T, Kline> priceMap, long timestamp) {
        priceMap.forEach((a, b) -> this.sell(a, b.getClose(), 100, timestamp, "回放时间结束，卖出所有持仓"));
        TradeDetail<T> tradeDetail = new TradeDetail<>();
        tradeDetail.setBalance(balance);
        tradeDetail.setOrders(orders);
        tradeDetail.setInitialBalance(initialBalance);
        tradeDetail.setFeeCount(feeCount);
        tradeDetail.setRecords(records);
        return tradeDetail;
    }
}
