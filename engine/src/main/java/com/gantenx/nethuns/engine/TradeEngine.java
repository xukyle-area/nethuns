package com.gantenx.nethuns.engine;

import java.util.List;
import java.util.Map;
import com.gantenx.nethuns.commons.enums.Proportion;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.engine.config.TradingConfig;
import com.gantenx.nethuns.engine.manager.*;
import com.gantenx.nethuns.engine.model.Order;
import com.gantenx.nethuns.engine.model.Position;
import com.gantenx.nethuns.engine.model.Trade;
import com.gantenx.nethuns.engine.model.TradeRecord;
import com.gantenx.nethuns.engine.provider.KlinePriceProvider;
import com.gantenx.nethuns.engine.provider.PriceProvider;
import com.gantenx.nethuns.engine.result.SellResult;

/**
 * 重构后的交易引擎
 * 使用组件化架构，职责分离清晰
 */
public class TradeEngine {

    // 组件管理器
    private final TimeManager timeManager;
    private final BalanceManager balanceManager;
    private final FeeCalculator feeCalculator;
    private final PriceProvider priceProvider;
    private final PositionManager positionManager;
    private final OrderManager orderManager;
    private final TradingConfig config;

    /**
     * 构造器
     *
     * @param timestamps 交易时间序列
     * @param klineMap   K线数据
     */
    public TradeEngine(List<Long> timestamps, Map<Symbol, Map<Long, Candle>> klineMap) {
        this(timestamps, klineMap, new TradingConfig());
    }

    /**
     * 构造器（带配置）
     *
     * @param timestamps 交易时间序列
     * @param klineMap   K线数据
     * @param config     交易配置
     */
    public TradeEngine(List<Long> timestamps, Map<Symbol, Map<Long, Candle>> klineMap, TradingConfig config) {
        this.config = config;
        this.timeManager = new TimeManager(timestamps);
        this.balanceManager = new BalanceManager(config.getInitialBalance(), config.getEpsilon());
        this.feeCalculator = new FeeCalculator(config.getFeeRate());
        this.priceProvider = new KlinePriceProvider(klineMap);
        this.positionManager = new PositionManager(config.getEpsilon());
        this.orderManager = new OrderManager();
    }

    /**
     * 是否还有下一个交易周期
     */
    public boolean hasNext() {
        return timeManager.hasNext();
    }

    /**
     * 开启下一个交易周期
     */
    public long next() {
        return timeManager.next();
    }

    /**
     * 按比例买入
     *
     * @param symbol     交易标的
     * @param proportion 买入比例
     */
    public void buy(Symbol symbol, Proportion proportion) {
        validateTrading();

        double amount = balanceManager.getBalance() * proportion.getValue() / 100.0;
        double price = getCurrentPrice(symbol);
        double maxQuantity = feeCalculator.calculateMaxQuantity(amount, price);

        buy(symbol, maxQuantity);
    }

    /**
     * 买入指定数量
     *
     * @param symbol   交易标的
     * @param quantity 买入数量
     */
    public void buy(Symbol symbol, double quantity) {
        validateTrading();

        if (quantity <= 0) {
            return;
        }

        double price = getCurrentPrice(symbol);
        double totalCost = feeCalculator.calculateTotalCost(quantity, price);

        if (balanceManager.canAfford(totalCost)) {
            balanceManager.deduct(totalCost);

            Order buyOrder = orderManager.createBuyOrder(symbol, quantity, price, timeManager.getCurrentTimestamp());
            positionManager.addPosition(symbol, buyOrder);
        }
    }

    /**
     * 按比例卖出
     *
     * @param symbol     交易标的
     * @param proportion 卖出比例
     */
    public void sell(Symbol symbol, Proportion proportion) {
        validateTrading();

        double totalQuantity = positionManager.getTotalQuantity(symbol);
        if (totalQuantity <= 0) {
            return;
        }

        double sellQuantity = totalQuantity * proportion.getValue() / 100.0;
        sell(symbol, sellQuantity);
    }

    /**
     * 卖出指定数量
     *
     * @param symbol   交易标的
     * @param quantity 卖出数量
     */
    public void sell(Symbol symbol, double quantity) {
        validateTrading();

        if (quantity <= 0) {
            return;
        }

        double price = getCurrentPrice(symbol);
        Order sellOrder = orderManager.createSellOrder(symbol, quantity, price, timeManager.getCurrentTimestamp());

        SellResult result = positionManager.sellQuantity(symbol, quantity, price, sellOrder.getOrderId(),
                timeManager.getCurrentTimestamp());

        // 将交易记录添加到订单管理器
        for (Trade trade : result.getTrades()) {
            orderManager.addTrade(trade.getBuyOrderId(), trade.getSellOrderId(), trade.getSymbol(), trade.getQuantity(),
                    trade.getBuyPrice(), trade.getSellPrice(), trade.getBuyTime(), trade.getSellTime(), trade.getCost(),
                    trade.getRevenue(), trade.getProfit());
        }

        // 将收入添加到余额
        double totalRevenue = result.getTrades().stream()
                .mapToDouble(trade -> feeCalculator.calculateRevenue(trade.getQuantity(), price)).sum();
        balanceManager.add(totalRevenue);
    }

    /**
     * 按金额卖出
     *
     * @param symbol 交易标的
     * @param amount 卖出金额
     */
    public void sellAmount(Symbol symbol, double amount) {
        validateTrading();

        if (amount <= 0) {
            return;
        }

        double price = getCurrentPrice(symbol);
        double quantity = amount / price;
        sell(symbol, quantity);
    }

    /**
     * 获取当前余额
     */
    public double getBalance() {
        return balanceManager.getBalance();
    }

    /**
     * 获取指定标的的持仓数量
     */
    public double getQuantity(Symbol symbol) {
        return positionManager.getTotalQuantity(symbol);
    }

    /**
     * 获取指定标的的所有仓位
     */
    public List<Position> getPositions(Symbol symbol) {
        return positionManager.getPositions(symbol);
    }

    /**
     * 获取持仓总价值
     */
    public double getPositionAsset() {
        return positionManager.getTotalPositionValue(this::getCurrentPrice);
    }

    /**
     * 结束交易，清仓并生成交易记录
     */
    public TradeRecord exit() {
        // 清仓所有持仓
        for (Symbol symbol : positionManager.getHeldSymbols()) {
            sell(symbol, Proportion.PROPORTION_OF_100);
        }

        // 生成交易记录
        TradeRecord tradeRecord = new TradeRecord();
        tradeRecord.setBalance(balanceManager.getBalance());
        tradeRecord.setInitialBalance(balanceManager.getInitialBalance());
        tradeRecord.setFeeCount(feeCalculator.getTotalFees());
        tradeRecord.setOrders(orderManager.getAllOrders());

        // 获取所有交易记录（从订单管理器中获取）
        List<Trade> allTrades = orderManager.getAllTrades();
        tradeRecord.setRecords(allTrades);

        return tradeRecord;
    }

    /**
     * 获取当前价格
     */
    private double getCurrentPrice(Symbol symbol) {
        return priceProvider.getPrice(symbol, timeManager.getCurrentTimestamp());
    }

    /**
     * 验证交易状态
     */
    private void validateTrading() {
        // 这里可以添加交易前的验证逻辑
        // 例如检查是否在交易时间内等
    }

    /**
     * 获取配置信息
     */
    public TradingConfig getConfig() {
        return config;
    }

    /**
     * 获取交易统计信息
     */
    public String getStatistics() {
        return String.format(
                "TradeEngine Statistics:\n" + "  %s\n" + "  %s\n" + "  %s\n" + "  %s\n" + "  %s\n"
                        + "  Progress: %.1f%%",
                balanceManager.toString(), feeCalculator.toString(), positionManager.toString(),
                orderManager.toString(), timeManager.toString(), timeManager.getProgress());
    }
}
