package com.gantenx;

import com.gantenx.model.Order;
import com.gantenx.model.RSI;
import com.gantenx.service.QuoteService;
import com.gantenx.strategy.RsiStrategy;
import com.gantenx.util.TradingChart;
import org.jfree.ui.RefineryUtilities;

import java.util.List;

public class Main {

    private static final QuoteService QUOTE_SERVICE = new QuoteService();


    public static void main(String[] args) {
        String symbol = "BTCUSDT";
        RsiStrategy rsiStrategy = new RsiStrategy();
        List<RSI> rsiList = QUOTE_SERVICE.getRsiList(symbol, "20230101", "20241201");
        List<Order> orderList = rsiStrategy.process(rsiList);
        TradingChart chart = new TradingChart(symbol, rsiList, orderList);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}
