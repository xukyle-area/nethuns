package com.gantenx;

import com.gantenx.model.Order;
import com.gantenx.model.RSI;
import com.gantenx.service.QuoteService;
import com.gantenx.strategy.RsiStrategy;
import com.gantenx.util.DateUtils;
import com.gantenx.util.TradingChart;
import org.jfree.ui.RefineryUtilities;

import java.util.List;

import static com.gantenx.constant.Constants.ONE_DAY;

public class Main {

    private static List<RSI> getRsiList(String symbol) {
        QuoteService quoteService = new QuoteService();
        long begin = DateUtils.getTimestamp("20240101");
        long end = DateUtils.getTimestamp("20241215");
        return quoteService.getRsi(symbol, ONE_DAY, begin, end, 1500);
    }

    public static void main(String[] args) {
        String symbol = "BTCUSDT";
        RsiStrategy rsiStrategy = new RsiStrategy();
        List<RSI> rsiList = getRsiList(symbol);
        List<Order> orderList = rsiStrategy.process(rsiList);

        TradingChart chart = new TradingChart(symbol, rsiList, orderList);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}
