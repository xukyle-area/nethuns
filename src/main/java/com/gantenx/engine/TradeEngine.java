package com.gantenx.engine;

import com.gantenx.constant.Side;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.constant.Side.BUY;
import static com.gantenx.constant.Side.SELL;

@Slf4j
public class TradeEngine {
    private static final double EPSILON = 1e-6;
    private static final double BALANCE_THRESHOLD = 0.001;

    private final double initialBalance;
    private final double fee;
    private final Map<Symbol, Position> positions = new HashMap<>();
    private final List<Order> orders = new ArrayList<>();

    private long nextOrderId = 0;
    private long nextTransaction = 0;
    private double balance;
    private double feeCount;

    public TradeEngine(double initialBalance, double fee) {
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.fee = fee;
        this.feeCount = 0.0;
    }

    public boolean hasNoPosition() {
        return positions.values().stream().noneMatch(position -> position.getQuantity() > 0);
    }

    public boolean hasPosition(Symbol symbol) {
        Position position1 = positions.get(symbol);
        return Objects.nonNull(position1) && position1.getQuantity() > 0;
    }

    public void buy(Symbol symbol, double price, double quantity, long ts) {
        if (quantity <= 0 || price <= 0) {
            log.warn("Invalid buy parameters: price={}, quantity={}", price, quantity);
            return;
        }

        double cost = calculateTotalCost(price, quantity);
        if (balance + EPSILON >= cost) {
            balance -= cost;
            updatePosition(symbol, price, quantity, BUY);
            // log.info("Bought {} {} at price {}, cost: {}, remaining balance: {}", quantity, symbol, price, cost, balance);
            orders.add(new Order(symbol, BUY, price, quantity, ts));
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

    public void sell(Symbol symbol, double price, double quantity, long ts) {
        if (quantity <= 0 || price <= 0) {
            log.warn("Invalid sell parameters: price={}, quantity={}", price, quantity);
            return;
        }

        Position position = positions.get(symbol);
        if (position == null || position.getQuantity() < quantity) {
            log.warn("Insufficient position to sell {}: requested={}, available={}", symbol, quantity, position != null ? position.getQuantity() : 0);
            return;
        }

        // 计算本次卖出的总收入（扣除手续费后）
        double revenue = calculateTotalRevenue(price, quantity);
        double profit = revenue - position.getAveragePrice() * quantity; // 本次交易的盈利

        // 更新余额
        balance += revenue;

        // log.info("Sold {} {} at price {}, revenue: {}, profit: {}, new balance: {}", quantity, symbol, price, revenue, profit, balance);

        // 更新仓位
        this.updatePosition(symbol, price, quantity, SELL);

        // 记录订单
        orders.add(new Order(symbol, SELL, price, quantity, ts));
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
        Position position = positions.get(symbol);
        if (position != null && position.getQuantity() > 0) {
            sell(symbol, price, position.getQuantity(), ts);
        }
    }

    public TradeDetail exit(Map<Symbol, Kline> priceMap, long ts) {
        priceMap.forEach((a, b) -> this.sellAll(a, b.getClose(), ts));
        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBalance(balance);
        tradeDetail.setOrders(new ArrayList<>(orders));
        tradeDetail.setInitialBalance(initialBalance);
        tradeDetail.setFeeCount(feeCount);
        tradeDetail.setPositionMap(getPositionSnapshot());
        return tradeDetail;
    }

    private void updatePosition(Symbol symbol, double price, double quantity, Side type) {
        Position position = positions.getOrDefault(symbol, new Position(0, 0));
        if (BUY.equals(type)) {
            position.addPosition(price, quantity);
        } else if (SELL.equals(type)) {
            position.reducePosition(quantity);
        }
        positions.put(symbol, position);
    }

    private double calculateTotalRevenue(double price, double quantity) {
        double revenue = price * quantity;
        double curFee = revenue * fee;
        feeCount += curFee;
        return revenue - curFee;
    }

    public Map<Symbol, Position> getPositionSnapshot() {
        return new HashMap<>(positions);
    }

    private long nextOrderId() {
        long curOrderId = nextOrderId;
        nextOrderId++;
        return curOrderId;
    }

    private long nextTranslationId() {
        long curTransactionId = nextTransaction;
        nextTransaction++;
        return curTransactionId;
    }
}
