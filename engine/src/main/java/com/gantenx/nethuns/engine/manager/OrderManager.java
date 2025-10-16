package com.gantenx.nethuns.engine.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.gantenx.nethuns.commons.enums.Side;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.engine.model.Order;
import com.gantenx.nethuns.engine.model.Trade;

/**
 * 订单管理器
 * 负责管理交易订单和生成订单ID
 */
public class OrderManager {

    private final List<Order> orders = new ArrayList<>();
    private final List<Trade> trades = new ArrayList<>();
    private long orderIdCounter = 0;
    private long tradeIdCounter = 0;

    /**
     * 创建买入订单
     *
     * @param symbol    交易标的
     * @param quantity  数量
     * @param price     价格
     * @param timestamp 时间戳
     * @return 新创建的订单
     */
    public Order createBuyOrder(Symbol symbol, double quantity, double price, long timestamp) {
        return createOrder(symbol, Side.BUY, quantity, price, timestamp);
    }

    /**
     * 创建卖出订单
     *
     * @param symbol    交易标的
     * @param quantity  数量
     * @param price     价格
     * @param timestamp 时间戳
     * @return 新创建的订单
     */
    public Order createSellOrder(Symbol symbol, double quantity, double price, long timestamp) {
        return createOrder(symbol, Side.SELL, quantity, price, timestamp);
    }

    /**
     * 创建订单
     *
     * @param symbol    交易标的
     * @param side      买卖方向
     * @param quantity  数量
     * @param price     价格
     * @param timestamp 时间戳
     * @return 新创建的订单
     */
    public Order createOrder(Symbol symbol, Side side, double quantity, double price, long timestamp) {
        validateOrderParameters(symbol, side, quantity, price, timestamp);

        long orderId = generateOrderId();
        Order order = new Order(orderId, symbol, side, price, quantity, timestamp);
        orders.add(order);

        return order;
    }

    /**
     * 获取所有订单（防御性副本）
     *
     * @return 订单列表
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    /**
     * 获取指定标的的所有订单
     *
     * @param symbol 交易标的
     * @return 订单列表
     */
    public List<Order> getOrdersBySymbol(Symbol symbol) {
        if (symbol == null) {
            return new ArrayList<>();
        }

        return orders.stream().filter(order -> symbol.equals(order.getSymbol())).collect(Collectors.toList());
    }

    /**
     * 获取指定方向的所有订单
     *
     * @param side 买卖方向
     * @return 订单列表
     */
    public List<Order> getOrdersBySide(Side side) {
        if (side == null) {
            return new ArrayList<>();
        }

        return orders.stream().filter(order -> side.equals(order.getType())).collect(Collectors.toList());
    }

    /**
     * 根据订单ID查找订单
     *
     * @param orderId 订单ID
     * @return 订单，如果未找到返回null
     */
    public Order getOrderById(long orderId) {
        return orders.stream().filter(order -> order.getOrderId() == orderId).findFirst().orElse(null);
    }

    /**
     * 获取订单总数
     *
     * @return 订单总数
     */
    public int getTotalOrderCount() {
        return orders.size();
    }

    /**
     * 获取买入订单数量
     *
     * @return 买入订单数量
     */
    public int getBuyOrderCount() {
        return (int) orders.stream().filter(order -> Side.BUY.equals(order.getType())).count();
    }

    /**
     * 获取卖出订单数量
     *
     * @return 卖出订单数量
     */
    public int getSellOrderCount() {
        return (int) orders.stream().filter(order -> Side.SELL.equals(order.getType())).count();
    }

    /**
     * 清空所有订单
     */
    public void clearAllOrders() {
        orders.clear();
    }

    /**
     * 获取交易的标的列表
     *
     * @return 标的列表
     */
    public List<Symbol> getTradedSymbols() {
        return orders.stream().map(Order::getSymbol).distinct().collect(Collectors.toList());
    }

    /**
     * 添加交易记录
     *
     * @param buyOrderId  买入订单ID
     * @param sellOrderId 卖出订单ID
     * @param symbol      交易标的
     * @param quantity    数量
     * @param buyPrice    买入价格
     * @param sellPrice   卖出价格
     * @param buyTime     买入时间
     * @param sellTime    卖出时间
     * @param cost        成本
     * @param revenue     收入
     * @param profit      利润
     */
    public void addTrade(long buyOrderId, long sellOrderId, Symbol symbol, double quantity, double buyPrice,
            double sellPrice, long buyTime, long sellTime, double cost, double revenue, double profit) {
        Trade trade = new Trade();
        trade.setId(++tradeIdCounter);
        trade.setBuyOrderId(buyOrderId);
        trade.setSellOrderId(sellOrderId);
        trade.setSymbol(symbol);
        trade.setQuantity(quantity);
        trade.setBuyPrice(buyPrice);
        trade.setSellPrice(sellPrice);
        trade.setBuyTime(buyTime);
        trade.setSellTime(sellTime);
        trade.setHoldDays(DateUtils.getDaysBetween(buyTime, sellTime));
        trade.setCost(cost);
        trade.setRevenue(revenue);
        trade.setProfit(profit);
        trade.setProfitRate(sellPrice / buyPrice);

        trades.add(trade);
    }

    /**
     * 获取所有交易记录
     */
    public List<Trade> getAllTrades() {
        return new ArrayList<>(trades);
    }

    /**
     * 获取交易记录总数
     */
    public int getTradeCount() {
        return trades.size();
    }

    /**
     * 生成新的订单ID
     *
     * @return 新的订单ID
     */
    private long generateOrderId() {
        return ++orderIdCounter;
    }

    /**
     * 验证订单参数
     */
    private void validateOrderParameters(Symbol symbol, Side side, double quantity, double price, long timestamp) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + price);
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive: " + timestamp);
        }
        if (Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            throw new IllegalArgumentException("Quantity must be a valid number: " + quantity);
        }
        if (Double.isNaN(price) || Double.isInfinite(price)) {
            throw new IllegalArgumentException("Price must be a valid number: " + price);
        }
    }

    @Override
    public String toString() {
        return String.format("OrderManager{totalOrders=%d, buyOrders=%d, sellOrders=%d, symbols=%d}",
                getTotalOrderCount(), getBuyOrderCount(), getSellOrderCount(), getTradedSymbols().size());
    }
}
