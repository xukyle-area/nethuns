package com.gantenx.nethuns.engine.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.gantenx.nethuns.engine.model.Trade;

/**
 * 卖出操作结果
 * 包含卖出生成的交易记录和实际卖出数量
 */
public class SellResult {

    private final List<Trade> trades;
    private final double soldQuantity;

    /**
     * 构造器
     *
     * @param trades       交易记录列表
     * @param soldQuantity 实际卖出数量
     */
    public SellResult(List<Trade> trades, double soldQuantity) {
        this.trades = trades != null ? new ArrayList<>(trades) : new ArrayList<>();
        this.soldQuantity = soldQuantity;
    }

    /**
     * 创建空的卖出结果
     *
     * @return 空的卖出结果
     */
    public static SellResult empty() {
        return new SellResult(Collections.emptyList(), 0.0);
    }

    /**
     * 获取交易记录列表（防御性副本）
     *
     * @return 交易记录列表
     */
    public List<Trade> getTrades() {
        return new ArrayList<>(trades);
    }

    /**
     * 获取实际卖出数量
     *
     * @return 实际卖出数量
     */
    public double getSoldQuantity() {
        return soldQuantity;
    }

    /**
     * 获取交易记录数量
     *
     * @return 交易记录数量
     */
    public int getTradeCount() {
        return trades.size();
    }

    /**
     * 是否为空结果
     *
     * @return true如果没有任何交易
     */
    public boolean isEmpty() {
        return trades.isEmpty() && soldQuantity == 0.0;
    }

    /**
     * 计算总收入
     *
     * @return 所有交易的总收入
     */
    public double getTotalRevenue() {
        return trades.stream().mapToDouble(Trade::getRevenue).sum();
    }

    /**
     * 计算总利润
     *
     * @return 所有交易的总利润
     */
    public double getTotalProfit() {
        return trades.stream().mapToDouble(Trade::getProfit).sum();
    }

    @Override
    public String toString() {
        return String.format("SellResult{trades=%d, soldQuantity=%.6f, totalRevenue=%.2f, totalProfit=%.2f}",
                trades.size(), soldQuantity, getTotalRevenue(), getTotalProfit());
    }
}
