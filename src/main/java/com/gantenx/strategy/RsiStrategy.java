package com.gantenx.strategy;

import com.gantenx.model.Order;
import com.gantenx.model.RSI;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RsiStrategy {

    public List<Order> process(List<RSI> rsiList) {
        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (RSI kLineWithRsi : rsiList) {
            Double rsi = kLineWithRsi.getRsi();
            if (rsi == null) {
                continue;
            }
            double price = Double.parseDouble(kLineWithRsi.getClosePrice());
            double position = tradeMocker.getPosition();
            double balance = tradeMocker.getBalance();
            long closeTime = kLineWithRsi.getCloseTime();
            if (rsi > 70 && position * price > 0.1) {
                tradeMocker.sellAll(price, closeTime);
            }
            if (rsi < 30 && balance > 0.1 * price) {
                tradeMocker.buyAll(price, closeTime);
            }
        }
        RSI rsi = rsiList.get(rsiList.size() - 1);
        double closePrice = Double.parseDouble(rsi.getClosePrice());
        tradeMocker.sellAll(closePrice, rsi.getCloseTime());
        return tradeMocker.getOrders();
    }
}
