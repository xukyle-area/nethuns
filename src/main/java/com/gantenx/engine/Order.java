package com.gantenx.engine;

import com.gantenx.annotation.ExcelColumn;
import com.gantenx.constant.Side;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Time;

public class Order extends Time {
    private int orderId;
    private final Side type;       // "buy" or "sell"
    private final double price;      // 价格
    @ExcelColumn(need = false)
    private final double quantity;   // 数量
    private final String symbol;    // 标的符号

    public Order(Symbol symbol, Side side, double price, double quantity, long timestamp) {
        super(timestamp);
        this.symbol = symbol.name();
        this.type = side;
        this.price = price;
        this.quantity = quantity;
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

    public String getSymbol() {
        return this.symbol;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
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
