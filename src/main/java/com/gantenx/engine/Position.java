package com.gantenx.engine;

import com.gantenx.constant.Period;

import java.util.List;

import static com.gantenx.utils.DateUtils.MS_OF_ONE_DAY_DOUBLE;

public class Position {
    // 买入的时候的订单id
    private long orderId;
    // 买入价格
    private double price;
    // 持仓数量
    private double quantity;
    // 买入时间
    private long timestamp;


    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Position() {
    }

    public Position(long orderId, double price, double quantity, long timestamp) {
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }


    /**
     * 计算持仓的平均价格
     *
     * @param positionList 持仓列表
     * @param timestamp    当前时间戳
     * @return 平均持仓价格，如果没有持仓返回0
     */
    public static double getAveragePrice(List<Position> positionList, long timestamp) {
        if (positionList == null || positionList.isEmpty()) {
            return 0;
        }

        double totalValue = 0;
        double totalQuantity = 0;

        for (Position position : positionList) {
            if (position.getQuantity() > 0 && position.getTimestamp() <= timestamp) {
                totalValue += position.getPrice() * position.getQuantity();
                totalQuantity += position.getQuantity();
            }
        }

        return totalQuantity > 0 ? totalValue / totalQuantity : 0;
    }

    /**
     * 计算平均持仓天数
     *
     * @param positionList 持仓列表
     * @param timestamp    当前时间戳
     * @return 平均持仓天数，如果没有持仓返回0
     */
    public static double getAverageHoldingDays(List<Position> positionList, long timestamp) {
        if (positionList == null || positionList.isEmpty()) {
            return 0;
        }

        double totalDays = 0;
        double totalQuantity = 0;

        for (Position position : positionList) {
            if (position.getQuantity() > 0 && position.getTimestamp() <= timestamp) {
                // 将毫秒转换为天
                double holdingDays = (double) (timestamp - position.getTimestamp()) / (Period.ONE_DAY.getMillisecond());
                totalDays += holdingDays * position.getQuantity();
                totalQuantity += position.getQuantity();
            }
        }

        return totalQuantity > 0 ? totalDays / totalQuantity : 0;
    }
}