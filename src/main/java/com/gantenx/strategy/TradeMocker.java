package com.gantenx.strategy;

import com.gantenx.model.Order;
import com.gantenx.model.TradeDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;

import java.util.*;

@Slf4j
public class TradeMocker {
    private double balance;
    private final double initialBalance;
    private final double fee;
    private double feeCount;
    private static final double EPSILON = 1e-6;


    // 仓位管理：标的 -> 仓位
    private final Map<String, Position> positionMap = new HashMap<>();

    // 订单记录
    private final List<Order> orders = new ArrayList<>();

    // 余额阈值，用于全仓买入时的限制
    private static final double BALANCE_THRESHOLD = 0.001;

    public TradeMocker(double initialBalance, double fee) {
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.fee = fee;
        this.feeCount = 0.0;
    }

    public boolean hasPosition() {
        return positionMap.values().stream().anyMatch(position -> position.getQuantity() > 0);
    }

    public void buy(String symbol, double price, double quantity, long ts) {
        if (quantity <= 0 || price <= 0) {
            log.warn("Invalid buy parameters: price={}, quantity={}", price, quantity);
            return;
        }

        double cost = calculateTotalCost(price, quantity);
        if (balance + EPSILON >= cost) {
            balance -= cost;
            updatePosition(symbol, price, quantity, "buy");
            log.info("Bought {} {} at price {}, cost: {}, remaining balance: {}", quantity, symbol, price, cost, balance);
            orders.add(new Order(symbol, "buy", price, quantity, ts));
        } else {
            log.warn("Insufficient balance to buy {}: cost={}, balance={}", symbol, cost, balance);
        }
    }

    private double calculateTotalCost(double price, double quantity) {
        double cost = price * quantity;
        double curFee = cost * fee;
        feeCount += curFee;
        return cost + curFee;
    }

    public void sell(String symbol, double price, double quantity, long ts) {
        if (quantity <= 0 || price <= 0) {
            log.warn("Invalid sell parameters: price={}, quantity={}", price, quantity);
            return;
        }

        Position position = positionMap.get(symbol);
        if (position == null || position.getQuantity() < quantity) {
            log.warn("Insufficient position to sell {}: requested={}, available={}", symbol, quantity, position != null ? position.getQuantity() : 0);
            return;
        }

        // 计算本次卖出的总收入（扣除手续费后）
        double revenue = calculateTotalRevenue(price, quantity);
        double profit = revenue - position.getAveragePrice() * quantity; // 本次交易的盈利

        // 更新余额
        balance += revenue;

        log.info("Sold {} {} at price {}, revenue: {}, profit: {}, new balance: {}", quantity, symbol, price, revenue, profit, balance);

        // 更新仓位
        updatePosition(symbol, price, quantity, "sell");

        // 记录订单
        orders.add(new Order(symbol, "sell", price, quantity, ts));
    }

    public void buyAll(String symbol, double price, long ts) {
        if (balance < initialBalance * BALANCE_THRESHOLD) {
            return;
        }

        double maxQuantity = balance / (price * (1 + fee));
        if (maxQuantity > 0) {
            buy(symbol, price, maxQuantity, ts);
        }
    }

    public void sellAll(String symbol, double price, long ts) {
        Position position = positionMap.get(symbol);
        if (position != null && position.getQuantity() > 0) {
            sell(symbol, price, position.getQuantity(), ts);
        }
    }

    public TradeDetail exit(Map<String, Double> priceMap, long ts) {
        priceMap.forEach((a, b) -> this.sellAll(a, b, ts));
        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBalance(balance);
        tradeDetail.setOrders(new ArrayList<>(orders));
        tradeDetail.setInitialBalance(initialBalance);
        tradeDetail.setFeeCount(feeCount);
        tradeDetail.setPositionMap(getPositionSnapshot());
        return tradeDetail;
    }

    private void updatePosition(String symbol, double price, double quantity, String type) {
        Position position = positionMap.getOrDefault(symbol, new Position(0, 0));
        if ("buy".equalsIgnoreCase(type)) {
            position.addPosition(price, quantity);
        } else if ("sell".equalsIgnoreCase(type)) {
            position.reducePosition(quantity);
        }
        positionMap.put(symbol, position);
    }

    private double calculateTotalRevenue(double price, double quantity) {
        double revenue = price * quantity;
        double curFee = revenue * fee;
        feeCount += curFee;
        return revenue - curFee;
    }

    private Map<String, Pair<Double, Double>> getPositionSnapshot() {
        Map<String, Pair<Double, Double>> snapshot = new HashMap<>();
        positionMap.forEach((symbol, position) -> snapshot.put(symbol, Pair.of(position.getAveragePrice(), position.getQuantity())));
        return snapshot;
    }
}
