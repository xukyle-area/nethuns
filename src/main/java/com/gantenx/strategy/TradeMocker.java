package com.gantenx.strategy;

import com.gantenx.model.Order;
import com.gantenx.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TradeMocker {
    private double balance;
    private final double initialBalance;
    private double position;
    private final double fee;
    private double feeCount;

    // 订单存储列表
    private final List<Order> orders;

    public TradeMocker(double initialBalance, double fee) {
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.fee = fee;
        this.position = 0.0;
        this.feeCount = 0.0;
        this.orders = new ArrayList<>();
    }

    /**
     * 模拟买入操作
     *
     * @param price    当前价格
     * @param quantity 买入数量
     * @param ts       时间戳
     */
    public void buy(double price, double quantity, long ts) {
        double cost = price * quantity;
        double curFee = cost * fee;
        feeCount += curFee;
        double totalCost = cost + curFee;

        if (balance >= totalCost) {
            balance -= totalCost;
            position += quantity;
            log.info("{} - buy, quantity:{}, price:{}，fee:{}, balance: {}, position: {}", DateUtils.getDate(ts), format(quantity), format(price), format(curFee), format(balance), format(position));

            // 存储买入订单
            orders.add(new Order("buy", price, quantity, ts));
        } else {
            log.info("balance not enough, cost:{}, balance{}", format(totalCost), format(balance));
        }
    }

    /**
     * 模拟卖出操作
     *
     * @param price    当前价格
     * @param quantity 卖出数量
     * @param ts       时间戳
     */
    public void sell(double price, double quantity, long ts) {
        if (quantity > position) {
            log.info("position not enough, quantity:{}, position{}", format(quantity), format(position));
        }
        double totalWithFee = price * quantity;
        double curFee = totalWithFee * fee;
        double totalRevenue = totalWithFee - curFee;

        position -= quantity;
        feeCount += curFee;
        balance += totalRevenue;
        log.info("{} - sell, quantity:{}, price:{}，fee:{}，balance: {}, position: {}", DateUtils.getDate(ts), format(quantity), format(price), format(curFee), format(balance), format(position));

        // 存储卖出订单
        orders.add(new Order("sell", price, quantity, ts));
    }

    /**
     * 全仓买入
     *
     * @param price 当前价格
     * @param ts    时间戳
     */
    public void buyAll(double price, long ts) {
        if (balance < initialBalance * 0.001) {
            return;
        }
        // 根据余额计算可购买数量
        double maxQuantity = balance / (price * (1 + 1.5 * fee));
        if (maxQuantity > 0) {
            buy(price, maxQuantity, ts);
        }
    }

    /**
     * 全仓卖出
     *
     * @param price 当前价格
     * @param ts    时间戳
     */
    public void sellAll(double price, long ts) {
        if (position > 0) {
            sell(price, position, ts);
        }
    }

    /**
     * 获取当前账户余额
     *
     * @return 当前余额
     */
    public double getBalance() {
        return balance;
    }

    /**
     * 获取当前持仓量
     *
     * @return 当前持仓量
     */
    public double getPosition() {
        return position;
    }

    /**
     * 获取累计手续费
     *
     * @return 累计手续费
     */
    public double getFeeCount() {
        return feeCount;
    }

    public double exit(double price, long ts) {
        sellAll(price, ts);
        double v = (this.balance - this.initialBalance) / this.initialBalance;
        log.info("收益率: {}", format(v));
        return v;
    }

    /**
     * 获取所有订单
     *
     * @return 所有订单
     */
    public List<Order> getOrders() {
        return orders;
    }

    private String format(double num) {
        DecimalFormat df = new DecimalFormat("#.#####");
        return df.format(num);
    }
}
