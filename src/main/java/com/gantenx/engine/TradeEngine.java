package com.gantenx.engine;

import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;

@Slf4j
public class TradeEngine {
    private final Map<Symbol, List<Position>> positions = new HashMap<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<TradeRecord> records = new ArrayList<>();
    private final List<Long> openDays;
    private final Map<Symbol, Map<Long, Kline>> klineMap;

    private double balance = INITIAL_BALANCE;
    private double feeCount = 0.0;
    private long orderId = 0;
    private long recordId = 0;
    private int openDayIndex = -1;
    private long timestamp;

    public Kline getKline(Symbol symbol) {
        return CollectionUtils.get(klineMap, symbol, timestamp);
    }

    public TradeEngine(List<Long> openDays, Map<Symbol, Map<Long, Kline>> klineMap) {
        if (openDays == null || openDays.isEmpty()) {
            throw new IllegalArgumentException("OpenDays cannot be null or empty");
        }
        if (klineMap == null || klineMap.isEmpty()) {
            throw new IllegalArgumentException("KlineMap cannot be null or empty");
        }
        this.openDays = openDays;
        this.klineMap = klineMap;
    }

    public boolean hasNextDay() {
        return openDayIndex + 1 < openDays.size();
    }

    public long nextDay() {
        if (!this.hasNextDay()) {
            return -1;
        }
        if (openDayIndex + 1 >= openDays.size()) {
            return -1; // 或抛出异常
        }
        openDayIndex++;
        timestamp = openDays.get(openDayIndex);
        return timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean hasPosition() {
        Collection<Position> positionList = CollectionUtils.toCollection(positions.values());
        return positionList.stream().anyMatch(position -> position.getQuantity() > 0);
    }

    public void sell(Symbol symbol, long proportion, String reason) {
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

        sell(symbol, sellQuantity, reason);
    }

    public void buy(Symbol symbol, long proportion, String reason) {
        double price = this.getPrice(symbol);
        double maxQuantity = (balance * proportion / 100) / (price * (1 + FEE));  // 计算可以买入的最大数量
        if (maxQuantity <= 0) {
            log.warn("{} - Insufficient balance to buy {}: balance={}", DateUtils.getDate(timestamp), symbol, balance);
            return;
        }
        buy(symbol, maxQuantity, reason);
    }

    public double getPrice(Symbol symbol) {
        Kline kline = this.getKline(symbol);
        if (Objects.isNull(kline)) {
            throw new IllegalArgumentException("Invalid kline getting parameters: symbol=" + symbol.name() + ", date=" + DateUtils.getDate(
                    timestamp));
        }
        return kline.getClose();
    }

    private void buy(Symbol symbol, double quantity, String reason) {
        double price = this.getPrice(symbol);
        if (quantity <= 0 || price <= 0) {
            log.warn("{} - Invalid buy parameters: price={}, quantity={}",
                     DateUtils.getDate(timestamp),
                     price,
                     quantity);
            return;
        }

        double cost = calculateTotalCost(symbol, quantity);
        if (balance + EPSILON >= cost) {
            balance -= cost;

            List<Position> positionList = positions.getOrDefault(symbol, new ArrayList<>());
            long orderId = generateOrderId();
            positionList.add(new Position(orderId, price, quantity, timestamp)); // 保存买入记录
            positions.put(symbol, positionList);

            orders.add(new Order(orderId, symbol, BUY, price, quantity, timestamp, reason));

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

    private void sell(Symbol symbol, double quantity, String reason) {
        double price = this.getPrice(symbol);
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
            double revenue = calculateTotalRevenue(symbol, sellQuantity);
            double profit = revenue - position.getPrice() * sellQuantity;

            totalRevenue += revenue;

            position.setQuantity(position.getQuantity() - sellQuantity);
            remainingQuantity -= sellQuantity;
            // 只在卖出的时候生成一条这个记录
            TradeRecord record = new TradeRecord();
            record.setId(generateRecordId());
            record.setBuyOrderId(position.getOrderId());
            record.setHoldDays(DateUtils.getDaysBetween(position.getTimestamp(), timestamp));
            record.setBuyPrice(position.getPrice());
            record.setBuyTime(position.getTimestamp());
            record.setSellOrderId(orderId);
            record.setSellPrice(price);
            record.setSellTime(timestamp);
            record.setQuantity(sellQuantity);
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
        orders.add(new Order(orderId, symbol, SELL, price, quantity, timestamp, reason));
    }

    private double calculateTotalCost(Symbol symbol, double quantity) {
        double cost = this.getPrice(symbol) * quantity;
        double curFee = cost * FEE;
        feeCount += curFee;
        return cost + curFee;
    }

    private double calculateTotalRevenue(Symbol symbol, double quantity) {
        double price = this.getPrice(symbol);
        double revenue = price * quantity;
        double curFee = revenue * FEE;
        feeCount += curFee;
        return revenue - curFee;
    }

    public double getQuantity(Symbol symbol) {
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

    public List<Position> getPositions(Symbol symbol) {
        List<Position> list = positions.get(symbol);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public double getBalance() {
        return this.balance;
    }

    public double getPositionAsset() {
        double asset = 0;
        for (Symbol Symbol : positions.keySet()) {
            double quantity = this.getQuantity(Symbol);
            asset += quantity * this.getPrice(Symbol);
        }
        return asset;
    }

    public TradeDetail exit() {
        for (Symbol Symbol : klineMap.keySet()) {
            this.sell(Symbol, 100, "回放时间结束，卖出所有持仓");
        }
        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBalance(balance);
        tradeDetail.setOrders(orders);
        tradeDetail.setInitialBalance(INITIAL_BALANCE);
        tradeDetail.setFeeCount(feeCount);
        tradeDetail.setRecords(records);
        return tradeDetail;
    }
}
