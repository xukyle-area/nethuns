package com.gantenx.strategy;

import com.gantenx.model.Order;
import com.gantenx.model.RSI;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RsiStrategy {

    public List<Order> process(List<RSI> rsiList) {
        double benifit = 0;
        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (int i = 0; i < rsiList.size(); i++) {
            RSI kLineWithRsi = rsiList.get(i);
            Double rsi = kLineWithRsi.getRsi();
            if (rsi == null) {
                continue;
            }
            double price = Double.parseDouble(kLineWithRsi.getClosePrice());
            long closeTime = kLineWithRsi.getCloseTime();
            if (rsi > 70) {
                tradeMocker.sellAll(price, closeTime);
            }
            if (rsi < 30) {
                tradeMocker.buyAll(price, closeTime);
            }
            if (i == rsiList.size() - 1) {
                benifit = tradeMocker.exit(price, closeTime);
            }
        }
        return tradeMocker.getOrders();
    }
}
