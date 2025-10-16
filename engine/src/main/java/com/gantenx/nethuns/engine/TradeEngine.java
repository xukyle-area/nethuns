package com.gantenx.nethuns.engine;

import com.gantenx.nethuns.commons.enums.Proportion;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.engine.model.Order;
import com.gantenx.nethuns.commons.utils.CollectionUtils;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.engine.model.Position;
import com.gantenx.nethuns.engine.model.TradeRecord;
import com.gantenx.nethuns.engine.model.Trade;

import java.util.*;

import static com.gantenx.nethuns.commons.constant.Constants.*;
import static com.gantenx.nethuns.commons.enums.Side.BUY;
import static com.gantenx.nethuns.commons.enums.Side.SELL;

public class TradeEngine {
    /**
     * 当前持仓的仓位列表，每次买入都是单独的记录
     */
    private final Map<Symbol, List<Position>> positions = new HashMap<>();
    /**
     * 进行交易的订单列表
     */
    private final List<Order> orders = new ArrayList<>();
    /**
     * 买入和卖出之间的对应记录
     */
    private final List<Trade> records = new ArrayList<>();
    /**
     * 开放交易的时间戳列表
     */
    private final List<Long> timestampList;
    /**
     * 交易的标的的 k 线列表
     */
    private final Map<Symbol, Map<Long, Candle>> klineMap;

    /**
     * 初始金额
     */
    private double balance = INITIAL_BALANCE;
    /**
     * 手续费总计
     */
    private double feeCount = 0.0;
    /**
     * 下一个订单的订单 id
     */
    private long orderId = 0;
    /**
     * 下一个交易记录的 id
     */
    private long recordId = 0;
    /**
     * 时间戳对应的在列表中的 index
     */
    private int timestampIndex = -1;
    /**
     * 当前在进行中的交易的时间戳
     */
    private long timestamp;

    /**
     * 构造方法，构建一个全新的交易引擎
     *
     * @param timestamps 写入交易的时间
     * @param klineMap   写入交易的标的以及在对应的时间段的 K 线
     */
    public TradeEngine(List<Long> timestamps, Map<Symbol, Map<Long, Candle>> klineMap) {
        if (timestamps == null || timestamps.isEmpty()) {
            throw new IllegalArgumentException("Trading time list cannot be null or empty");
        }
        if (klineMap == null || klineMap.isEmpty()) {
            throw new IllegalArgumentException("KlineMap cannot be null or empty");
        }
        this.timestampList = timestamps;
        this.klineMap = klineMap;
    }

    /**
     * 是否存在下一个交易周期
     */
    public boolean hasNext() {
        return timestampIndex + 1 < timestampList.size();
    }

    /**
     * 开启下一个交易周期
     */
    public long next() {
        if (!this.hasNext()) {
            return -1;
        }
        if (timestampIndex + 1 >= timestampList.size()) {
            return -1; // 或抛出异常
        }
        timestampIndex++;
        timestamp = timestampList.get(timestampIndex);
        return timestamp;
    }

    /**
     * 卖出
     *
     * @param symbol     标的
     * @param proportion 占有现在仓位的比例
     */
    public void sell(Symbol symbol, Proportion proportion) {
        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            return;
        }

        double totalQuantity = positionList.stream().mapToDouble(Position::getQuantity).sum();
        double sellQuantity = totalQuantity * proportion.getValue() / 100;

        if (sellQuantity <= 0) {
            return;
        }

        this.sell(symbol, sellQuantity);
    }

    /**
     * 卖出
     *
     * @param symbol 标的
     * @param amount 成交额
     */
    public void sellAmount(Symbol symbol, double amount) {
        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            return;
        }
        double price = this.getPrice(symbol);
        double sellQuantity = amount / price;

        this.sell(symbol, sellQuantity);
    }

    public void buy(Symbol symbol, Proportion proportion) {
        double amount = this.balance * proportion.getValue() / 100;
        double maxQuantity = this.getMaxQuantity(symbol, amount);
        this.buy(symbol, maxQuantity);
    }

    public double getQuantity(Symbol symbol) {
        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            return 0d;
        }

        Optional<Double> optional = positionList.stream().map(Position::getQuantity).reduce(Double::sum);
        return optional.orElse(0d);
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

    /**
     * 结束交易，卖出所有持仓，记录结果并导出
     */
    public TradeRecord exit() {
        for (Symbol Symbol : klineMap.keySet()) {
            this.sell(Symbol, Proportion.PROPORTION_OF_100);
        }
        TradeRecord tradeRecord = new TradeRecord();
        tradeRecord.setBalance(balance);
        tradeRecord.setOrders(orders);
        tradeRecord.setInitialBalance(INITIAL_BALANCE);
        tradeRecord.setFeeCount(feeCount);
        tradeRecord.setRecords(records);
        return tradeRecord;
    }

    public double getPrice(Symbol symbol) {
        Candle kline = CollectionUtils.get(klineMap, symbol, timestamp);
        if (Objects.isNull(kline)) {
            throw new IllegalArgumentException("Invalid kline getting parameters: symbol=" + symbol.name() + ", date=" + DateUtils.getDate(
                    timestamp));
        }
        return kline.getOpen();
    }

    /**
     * 买入的股数
     *
     * @param symbol   币对
     * @param quantity 数量
     */
    public void buy(Symbol symbol, double quantity) {
        double price = this.getPrice(symbol);
        if (quantity <= 0 || price <= 0) {
            return;
        }

        double cost = this.calculateTotalCost(symbol, quantity);
        if (cost >= 1 && balance + EPSILON >= cost) {
            balance -= cost;

            List<Position> positionList = positions.getOrDefault(symbol, new ArrayList<>());
            long orderId = generateOrderId();
            positionList.add(new Position(symbol, orderId, price, quantity, timestamp));
            positions.put(symbol, positionList);
            orders.add(new Order(orderId, symbol, BUY, price, quantity, timestamp));
        }
    }

    public void sell(Symbol symbol, double quantity) {
        double price = this.getPrice(symbol);
        if (quantity <= 0 || price <= 0) {
            return;
        }

        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            return;
        }

        double remainingQuantity = quantity;
        Iterator<Position> iterator = positionList.iterator();
        double totalRevenue = 0.0;

        long orderId = generateOrderId();
        while (iterator.hasNext() && remainingQuantity > 0) {
            Position position = iterator.next();
            if (Math.abs(position.getQuantity()) < EPSILON) {
                iterator.remove();
                continue;
            }
            double sellQuantity = Math.min(position.getQuantity(), remainingQuantity);

            double revenue = this.calculateRevenue(symbol, sellQuantity);
            totalRevenue += revenue;
            Trade record = this.buildTradeRecord(position, revenue, sellQuantity);
            records.add(record);

            double newQuantity = position.getQuantity() - sellQuantity;
            position.setQuantity(newQuantity);
            remainingQuantity -= sellQuantity;
            if (Math.abs(newQuantity) < EPSILON) {
                iterator.remove();
            }
        }
        balance += totalRevenue;
        orders.add(new Order(orderId, symbol, SELL, price, quantity, timestamp));
    }

    private Trade buildTradeRecord(Position position, double revenue, double sellQuantity) {
        Symbol symbol = position.getSymbol();
        double price = this.getPrice(symbol);

        double buyPrice = position.getPrice();
        double profit = revenue - buyPrice * sellQuantity;
        Trade record = new Trade();
        record.setId(generateRecordId());
        record.setBuyOrderId(position.getOrderId());
        record.setHoldDays(DateUtils.getDaysBetween(position.getTimestamp(), timestamp));
        record.setBuyPrice(buyPrice);
        record.setBuyTime(position.getTimestamp());
        record.setSellOrderId(orderId);
        record.setSellPrice(price);
        record.setSellTime(timestamp);
        record.setQuantity(sellQuantity);
        record.setSymbol(symbol);
        record.setProfit(profit);
        record.setProfitRate(price / buyPrice);
        record.setRevenue(revenue);
        record.setCost(sellQuantity * buyPrice);
        return record;
    }

    public double getMaxQuantity(Symbol symbol, double amount) {
        double price = this.getPrice(symbol);
        return amount / (price * (1 + FEE));
    }

    private double calculateTotalCost(Symbol symbol, double quantity) {
        double cost = this.getPrice(symbol) * quantity;
        double curFee = cost * FEE;
        feeCount += curFee;
        return cost + curFee;
    }

    private double calculateRevenue(Symbol symbol, double quantity) {
        double price = this.getPrice(symbol);
        double revenue = price * quantity;
        double curFee = revenue * FEE;
        feeCount += curFee;
        return revenue - curFee;
    }

    private long generateOrderId() {
        orderId++;
        return orderId;
    }

    private long generateRecordId() {
        recordId++;
        return recordId;
    }
}
