package com.gantenx.nethuns.engine.manager;

/**
 * 费用计算器
 * 负责所有交易费用相关的计算
 */
public class FeeCalculator {

    private final double feeRate;
    private double totalFees = 0.0;

    /**
     * 构造器
     *
     * @param feeRate 手续费率（0-1之间）
     */
    public FeeCalculator(double feeRate) {
        if (feeRate < 0 || feeRate > 1) {
            throw new IllegalArgumentException("Fee rate must be between 0 and 1");
        }
        this.feeRate = feeRate;
    }

    /**
     * 计算买入总成本（包含手续费）
     *
     * @param quantity 数量
     * @param price    单价
     * @return 总成本
     */
    public double calculateTotalCost(double quantity, double price) {
        validateQuantityAndPrice(quantity, price);

        double cost = quantity * price;
        double fee = cost * feeRate;
        totalFees += fee;
        return cost + fee;
    }

    /**
     * 计算卖出净收入（扣除手续费）
     *
     * @param quantity 数量
     * @param price    单价
     * @return 净收入
     */
    public double calculateRevenue(double quantity, double price) {
        validateQuantityAndPrice(quantity, price);

        double grossRevenue = quantity * price;
        double fee = grossRevenue * feeRate;
        totalFees += fee;
        return grossRevenue - fee;
    }

    /**
     * 根据可用资金计算可买入的最大数量
     *
     * @param availableAmount 可用资金
     * @param price          单价
     * @return 最大可买入数量
     */
    public double calculateMaxQuantity(double availableAmount, double price) {
        validateQuantityAndPrice(availableAmount, price);
        return availableAmount / (price * (1 + feeRate));
    }

    /**
     * 计算单笔交易的手续费
     *
     * @param quantity 数量
     * @param price    单价
     * @return 手续费金额
     */
    public double calculateFee(double quantity, double price) {
        validateQuantityAndPrice(quantity, price);
        return quantity * price * feeRate;
    }

    /**
     * 获取总手续费
     *
     * @return 累计手续费
     */
    public double getTotalFees() {
        return totalFees;
    }

    /**
     * 获取手续费率
     *
     * @return 手续费率
     */
    public double getFeeRate() {
        return feeRate;
    }

    /**
     * 重置累计手续费
     */
    public void resetTotalFees() {
        totalFees = 0.0;
    }

    /**
     * 验证数量和价格的有效性
     */
    private void validateQuantityAndPrice(double quantity, double price) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + price);
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
        return String.format("FeeCalculator{feeRate=%.4f, totalFees=%.2f}", feeRate, totalFees);
    }
}
