package com.gantenx.nethuns.engine.manager;

import java.util.*;
import java.util.stream.Collectors;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.engine.model.Order;
import com.gantenx.nethuns.engine.model.Position;
import com.gantenx.nethuns.engine.model.Trade;
import com.gantenx.nethuns.engine.result.SellResult;

/**
 * 仓位管理器
 * 负责管理交易仓位和生成交易记录
 */
public class PositionManager {

    private final Map<Symbol, List<Position>> positions = new HashMap<>();
    private final double epsilon;
    private long recordIdCounter = 0;

    /**
     * 构造器
     *
     * @param epsilon 精度值
     */
    public PositionManager(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * 添加新仓位
     *
     * @param symbol   交易标的
     * @param buyOrder 买入订单
     * @return 新建的仓位
     */
    public Position addPosition(Symbol symbol, Order buyOrder) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        if (buyOrder == null) {
            throw new IllegalArgumentException("Buy order cannot be null");
        }

        Position position = new Position(symbol, buyOrder.getOrderId(), buyOrder.getPrice(), buyOrder.getQuantity(),
                buyOrder.getTimestamp());

        positions.computeIfAbsent(symbol, k -> new ArrayList<>()).add(position);
        return position;
    }

    /**
     * 获取指定标的的所有仓位（防御性副本）
     *
     * @param symbol 交易标的
     * @return 仓位列表
     */
    public List<Position> getPositions(Symbol symbol) {
        if (symbol == null) {
            return new ArrayList<>();
        }

        return positions.getOrDefault(symbol, Collections.emptyList()).stream().map(this::copyPosition)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定标的的总持仓数量
     *
     * @param symbol 交易标的
     * @return 总持仓数量
     */
    public double getTotalQuantity(Symbol symbol) {
        if (symbol == null) {
            return 0.0;
        }

        return positions.getOrDefault(symbol, Collections.emptyList()).stream().mapToDouble(Position::getQuantity)
                .sum();
    }

    /**
     * 卖出指定数量的仓位
     *
     * @param symbol        交易标的
     * @param sellQuantity  卖出数量
     * @param sellPrice     卖出价格
     * @param sellOrderId   卖出订单ID
     * @param timestamp     卖出时间戳
     * @return 卖出结果
     */
    public SellResult sellQuantity(Symbol symbol, double sellQuantity, double sellPrice, long sellOrderId,
            long timestamp) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        if (sellQuantity <= 0) {
            return SellResult.empty();
        }
        if (sellPrice <= 0) {
            throw new IllegalArgumentException("Sell price must be positive");
        }

        List<Position> symbolPositions = positions.get(symbol);
        if (symbolPositions == null || symbolPositions.isEmpty()) {
            return SellResult.empty();
        }

        return executeSell(symbolPositions, sellQuantity, sellPrice, sellOrderId, timestamp);
    }

    /**
     * 获取所有持仓标的
     *
     * @return 持仓标的集合
     */
    public Set<Symbol> getHeldSymbols() {
        return positions.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * 清空所有仓位
     */
    public void clearAllPositions() {
        positions.clear();
    }

    /**
     * 获取持仓总价值
     *
     * @param priceFunction 价格获取函数
     * @return 持仓总价值
     */
    public double getTotalPositionValue(java.util.function.Function<Symbol, Double> priceFunction) {
        return positions.entrySet().stream().mapToDouble(entry -> {
            Symbol symbol = entry.getKey();
            double quantity = getTotalQuantity(symbol);
            double price = priceFunction.apply(symbol);
            return quantity * price;
        }).sum();
    }

    /**
     * 执行卖出操作
     */
    private SellResult executeSell(List<Position> positions, double sellQuantity, double sellPrice, long sellOrderId,
            long timestamp) {
        List<Trade> trades = new ArrayList<>();
        double remainingQuantity = sellQuantity;
        Iterator<Position> iterator = positions.iterator();

        while (iterator.hasNext() && remainingQuantity > epsilon) {
            Position position = iterator.next();

            // 跳过数量为0的仓位
            if (Math.abs(position.getQuantity()) < epsilon) {
                iterator.remove();
                continue;
            }

            double quantityToSell = Math.min(position.getQuantity(), remainingQuantity);

            // 创建交易记录
            Trade trade = createTrade(position, quantityToSell, sellPrice, sellOrderId, timestamp);
            trades.add(trade);

            // 更新仓位数量
            double newQuantity = position.getQuantity() - quantityToSell;
            position.setQuantity(newQuantity);
            remainingQuantity -= quantityToSell;

            // 如果仓位清空，从列表中移除
            if (Math.abs(newQuantity) < epsilon) {
                iterator.remove();
            }
        }

        return new SellResult(trades, sellQuantity - remainingQuantity);
    }

    /**
     * 创建交易记录
     */
    private Trade createTrade(Position position, double sellQuantity, double sellPrice, long sellOrderId,
            long timestamp) {
        Trade trade = new Trade();
        trade.setId(++recordIdCounter);
        trade.setSymbol(position.getSymbol());
        trade.setBuyOrderId(position.getOrderId());
        trade.setSellOrderId(sellOrderId);
        trade.setBuyPrice(position.getPrice());
        trade.setSellPrice(sellPrice);
        trade.setBuyTime(position.getTimestamp());
        trade.setSellTime(timestamp);
        trade.setQuantity(sellQuantity);
        trade.setHoldDays(DateUtils.getDaysBetween(position.getTimestamp(), timestamp));

        // 计算成本、收入和利润
        double cost = sellQuantity * position.getPrice();
        double revenue = sellQuantity * sellPrice;
        double profit = revenue - cost;

        trade.setCost(cost);
        trade.setRevenue(revenue);
        trade.setProfit(profit);
        trade.setProfitRate(sellPrice / position.getPrice());

        return trade;
    }

    /**
     * 创建仓位的防御性副本
     */
    private Position copyPosition(Position original) {
        return new Position(original.getSymbol(), original.getOrderId(), original.getPrice(), original.getQuantity(),
                original.getTimestamp());
    }

    @Override
    public String toString() {
        int totalPositions = positions.values().stream().mapToInt(List::size).sum();
        return String.format("PositionManager{symbols=%d, totalPositions=%d}", positions.size(), totalPositions);
    }
}
