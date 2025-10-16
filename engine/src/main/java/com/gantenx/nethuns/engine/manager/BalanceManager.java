package com.gantenx.nethuns.engine.manager;

import com.gantenx.nethuns.engine.exception.InsufficientFundsException;

/**
 * 余额管理器
 * 负责管理交易账户余额和资金操作
 */
public class BalanceManager {

    private double balance;
    private final double initialBalance;
    private final double epsilon;

    /**
     * 构造器
     *
     * @param initialBalance 初始余额
     * @param epsilon        精度值
     */
    public BalanceManager(double initialBalance, double epsilon) {
        if (initialBalance <= 0) {
            throw new IllegalArgumentException("Initial balance must be positive");
        }
        if (epsilon <= 0) {
            throw new IllegalArgumentException("Epsilon must be positive");
        }

        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.epsilon = epsilon;
    }

    /**
     * 检查是否有足够余额
     *
     * @param amount 需要的金额
     * @return true如果余额足够
     */
    public boolean canAfford(double amount) {
        validateAmount(amount);
        return balance + epsilon >= amount;
    }

    /**
     * 扣除金额
     *
     * @param amount 扣除的金额
     * @throws InsufficientFundsException 如果余额不足
     */
    public void deduct(double amount) {
        validateAmount(amount);
        if (!canAfford(amount)) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Required: %.2f, Available: %.2f", amount, balance));
        }
        balance -= amount;
    }

    /**
     * 增加金额
     *
     * @param amount 增加的金额
     */
    public void add(double amount) {
        validateAmount(amount);
        balance += amount;
    }

    /**
     * 获取当前余额
     *
     * @return 当前余额
     */
    public double getBalance() {
        return balance;
    }

    /**
     * 获取初始余额
     *
     * @return 初始余额
     */
    public double getInitialBalance() {
        return initialBalance;
    }

    /**
     * 获取盈亏金额
     *
     * @return 当前余额与初始余额的差值
     */
    public double getProfitLoss() {
        return balance - initialBalance;
    }

    /**
     * 获取盈亏比例
     *
     * @return 盈亏比例（百分比）
     */
    public double getProfitLossRate() {
        return (balance - initialBalance) / initialBalance * 100;
    }

    /**
     * 重置余额到初始状态
     */
    public void reset() {
        balance = initialBalance;
    }

    /**
     * 验证金额有效性
     */
    private void validateAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Amount must be a valid number: " + amount);
        }
    }

    @Override
    public String toString() {
        return String.format("BalanceManager{balance=%.2f, initial=%.2f, P&L=%.2f(%.2f%%)}", balance, initialBalance,
                getProfitLoss(), getProfitLossRate());
    }
}
