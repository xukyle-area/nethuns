package com.gantenx.engine;

import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;

@Slf4j
public class TradeEngine {

    private static final double EPSILON = 1e-6;
    private static final double BALANCE_THRESHOLD = 0.001;

    private final double initialBalance;
    private final double fee; // 交易手续费率
    private final Map<Symbol, List<Position>> positions = new HashMap<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<TradeRecord> records = new ArrayList<>();
    private double balance;
    private double feeCount;
    private long orderId = 0;
    private long recordId = 0;

    public TradeEngine(double initialBalance, double fee) {
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.fee = fee;
        this.feeCount = 0.0;
    }

    public boolean hasPosition(Symbol symbol) {
        List<Position> positionList = positions.get(symbol);
        if (CollectionUtils.isEmpty(positionList)) {
            return false;
        }
        for (Position position : positionList) {
            if (position.getQuantity() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNoPosition() {
        Collection<Position> positionList = CollectionUtils.toCollection(positions.values());
        return positionList.stream().noneMatch(position -> position.getQuantity() > 0);
    }

    /**
     * 按照余额的比例进行买入
     *
     * @param symbol     标的
     * @param price      价格
     * @param proportion 百分比
     * @param ts         时间戳
     */
    public void buy(Symbol symbol, double price, long proportion, long ts) {
        double maxQuantity = (balance * proportion / 100) / (price * (1 + fee));  // 计算可以买入的最大数量
        if (maxQuantity <= 0) {
            log.warn("Insufficient balance to buy {}: balance={}", symbol, balance);
            return;
        }
        buy(symbol, price, maxQuantity, ts);
    }

    // 依据指定价格和数量进行买入
    public void buy(Symbol symbol, double price, double quantity, long ts) {
        if (quantity <= 0 || price <= 0) {
            log.warn("Invalid buy parameters: price={}, quantity={}", price, quantity);
            return;
        }

        double cost = calculateTotalCost(price, quantity);
        if (balance >= cost) {
            balance -= cost;

            List<Position> positionList = positions.getOrDefault(symbol, new ArrayList<>());
            positionList.add(new Position(generateOrderId(), price, quantity, ts)); // 保存买入记录
            positions.put(symbol, positionList);

            orders.add(new Order(generateOrderId(), symbol, BUY, price, quantity, ts));

            log.info("Bought {} {} at price {}, cost: {}, remaining balance: {}", quantity, symbol, price, cost, balance);
        } else {
            log.warn("Insufficient balance to buy {}: cost={}, balance={}", symbol, cost, balance);
        }
    }

    /**
     * 按照持仓的比例进行卖出
     *
     * @param symbol     标的
     * @param price      价格
     * @param proportion 百分比
     * @param ts         时间戳
     */
    public void sell(Symbol symbol, double price, long proportion, long ts) {
        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            log.warn("No position to sell for {}", symbol);
            return;
        }

        double totalQuantity = positionList.stream().mapToDouble(Position::getQuantity).sum();
        double sellQuantity = totalQuantity * proportion / 100;

        if (sellQuantity <= 0) {
            log.warn("Invalid sell quantity: sellQuantity={}, totalQuantity={}", sellQuantity, totalQuantity);
            return;
        }

        sell(symbol, price, sellQuantity, ts);
    }

    // 卖出操作（根据数量卖出）
    public void sell(Symbol symbol, double price, double quantity, long timestamp) {
        if (quantity <= 0 || price <= 0) {
            log.warn("Invalid sell parameters: price={}, quantity={}", price, quantity);
            return;
        }

        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            log.warn("No position to sell for {}", symbol);
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

            TradeRecord record = new TradeRecord();
            record.setId(generateRecordId());
            record.setBuyOrderId(position.getOrderId());
            record.setBuyPrice(position.getPrice());
            record.setBuyTime(position.getTimestamp());
            record.setSellOrderId(orderId);
            record.setSellPrice(price);
            record.setSellTime(timestamp);
            record.setQuantity(quantity);
            record.setProfit(profit);
            records.add(record);

            // 如果仓位清空，移除该记录
            if (Math.abs(position.getQuantity()) < 1e-6) {
                iterator.remove();
            }
        }

        balance += totalRevenue;

        orders.add(new Order(orderId, symbol, SELL, price, quantity, timestamp));
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

    public void buyAll(Symbol symbol, double price, long ts) {
        if (balance < initialBalance * BALANCE_THRESHOLD) {
            return;
        }

        double maxQuantity = balance / (price * (1 + fee));
        if (maxQuantity > 0) {
            buy(symbol, price, maxQuantity, ts);
        }
    }

    public void sellAll(Symbol symbol, double price, long ts) {
        if (this.hasPosition(symbol)) {
            sell(symbol, price, this.getQuantity(symbol), ts);
        }
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

    public Map<Symbol, List<Position>> getPositions() {
        return new HashMap<>(positions);
    }

    public TradeDetail exit(Map<Symbol, Kline> priceMap, long ts) {
        priceMap.forEach((a, b) -> this.sell(a, b.getClose(), 100, ts));
        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBalance(balance);
        tradeDetail.setOrders(new ArrayList<>(orders));
        tradeDetail.setInitialBalance(initialBalance);
        tradeDetail.setFeeCount(feeCount);
        tradeDetail.setRecords(records);
        return tradeDetail;
    }
}
