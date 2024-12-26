package com.gantenx.nethuns.engine.model;


import com.gantenx.nethuns.commons.annotation.ExcelColumn;
import com.gantenx.nethuns.commons.constant.Symbol;

public class TradeRecord {
    private long id;
    private Symbol symbol;
    // 买入时间
    @ExcelColumn(dateFormat = "yyyy-MM-dd HH:mm:ss")
    private long buyTime;
    // 卖出时间
    @ExcelColumn(dateFormat = "yyyy-MM-dd HH:mm:ss")
    private long sellTime;
    // 持有天数
    @ExcelColumn(name = "days")
    private long holdDays;
    // 收益率
    private double profitRate;
    // 买入价格
    private double buyPrice;
    // 卖出价格
    private double sellPrice;
    // 交易数量（如果买入的时候和买出的订单，数量不同，那么生成条记录）
    private double quantity;
    // 收益
    private double profit;
    // 买入时候的订单id
    private long buyOrderId;
    // 卖出时候的订单id
    private long sellOrderId;
    private double revenue;
    private double cost;

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public long getHoldDays() {
        return holdDays;
    }

    public void setHoldDays(long holdDays) {
        this.holdDays = holdDays;
    }

    public long getBuyOrderId() {
        return buyOrderId;
    }

    public void setBuyOrderId(long buyOrderId) {
        this.buyOrderId = buyOrderId;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public void setSellOrderId(long sellOrderId) {
        this.sellOrderId = sellOrderId;
    }

    public long getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(long buyTime) {
        this.buyTime = buyTime;
    }

    public long getSellTime() {
        return sellTime;
    }

    public void setSellTime(long sellTime) {
        this.sellTime = sellTime;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public double getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(double profitRate) {
        this.profitRate = profitRate;
    }
}
