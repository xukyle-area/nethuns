package com.gantenx.engine;

import com.gantenx.constant.Proportion;
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
    private final List<TradeRecord> records = new ArrayList<>();
    /**
     * 开放交易的时间戳列表
     */
    private final List<Long> timestampList;
    /**
     * 交易的标的的 k 线列表
     */
    private final Map<Symbol, Map<Long, Kline>> klineMap;

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
    public TradeEngine(List<Long> timestamps, Map<Symbol, Map<Long, Kline>> klineMap) {
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
     * @param reason     原因
     */
    public void sell(Symbol symbol, Proportion proportion, String reason) {
        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            return;
        }

        double totalQuantity = positionList.stream().mapToDouble(Position::getQuantity).sum();
        double sellQuantity = totalQuantity * proportion.getValue() / 100;

        if (sellQuantity <= 0) {
            return;
        }

        this.sell(symbol, sellQuantity, reason);
    }

    /**
     * 卖出
     *
     * @param symbol 标的
     * @param amount 成交额
     * @param reason 原因
     */
    public void sellAmount(Symbol symbol, double amount, String reason) {
        List<Position> positionList = positions.get(symbol);
        if (positionList == null || positionList.isEmpty()) {
            return;
        }
        double price = this.getPrice(symbol);
        double sellQuantity = amount / price;

        this.sell(symbol, sellQuantity, reason);
    }

    public void buyAmount(Symbol symbol, double amount, String reason) {
        if (this.balance < amount) {
            amount = this.balance;
        }
        double maxQuantity = this.getMaxQuantity(symbol, amount);
        this.buy(symbol, maxQuantity, reason);
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
    public TradeDetail exit() {
        for (Symbol Symbol : klineMap.keySet()) {
            this.sell(Symbol, 100, "Time up, sell all");
        }
        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBalance(balance);
        tradeDetail.setOrders(orders);
        tradeDetail.setInitialBalance(INITIAL_BALANCE);
        tradeDetail.setFeeCount(feeCount);
        tradeDetail.setRecords(records);
        return tradeDetail;
    }

    public double getPrice(Symbol symbol) {
        Kline kline = CollectionUtils.get(klineMap, symbol, timestamp);
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
     * @param reason   买入原因
     */
    private void buy(Symbol symbol, double quantity, String reason) {
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
            orders.add(new Order(orderId, symbol, BUY, price, quantity, timestamp, reason));
        } else {
            log.info("下单失败:{},{},{}", symbol.name(), cost, this.balance);
        }
    }

    public void sell(Symbol symbol, double quantity, String reason) {
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
            double sellQuantity = Math.min(position.getQuantity(), remainingQuantity);

            double revenue = this.calculateRevenue(symbol, sellQuantity);
            totalRevenue += revenue;
            TradeRecord record = this.buildTradeRecord(position, revenue, sellQuantity);


            records.add(record);
            if (Math.abs(position.getQuantity()) < EPSILON) {
                iterator.remove();
            }
            position.setQuantity(position.getQuantity() - sellQuantity);
            remainingQuantity -= sellQuantity;
        }
        balance += totalRevenue;
        orders.add(new Order(orderId, symbol, SELL, price, quantity, timestamp, reason));

    }

    private TradeRecord buildTradeRecord(Position position, double revenue, double sellQuantity) {
        Symbol symbol = position.getSymbol();
        double price = this.getPrice(symbol);

        double profit = revenue - position.getPrice() * sellQuantity;
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
        record.setRevenue(revenue);
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
