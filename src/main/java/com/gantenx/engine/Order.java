package com.gantenx.engine;

import com.gantenx.constant.Side;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Time;

public class Order extends Time {
    private final Symbol symbol;    // 标的符号
    private final Side type;       // "buy" or "sell"
    private final double price;      // 价格
    private final double quantity;   // 数量
    private long orderId;
    private final String reason;

    public Order(long orderId, Symbol symbol, Side side, double price, double quantity, long timestamp, String reason) {
        super(timestamp);
        this.orderId = orderId;
        this.symbol = symbol;
        this.type = side;
        this.price = price;
        this.quantity = quantity;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public Side getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public Symbol getSymbol() {
        return this.symbol;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "type='" + type + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + super.getTimestamp() +
                '}';
    }
}
