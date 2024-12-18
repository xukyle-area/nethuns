package com.gantenx.strategy;

import com.gantenx.calculator.OrderCalculator;
import com.gantenx.model.Kline;
import com.gantenx.model.Order;
import com.gantenx.model.ProfitResult;
import com.gantenx.model.TradeDetail;
import com.gantenx.util.DateUtils;
import com.gantenx.util.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractQQQStrategy {

    protected final double initialBalance;
    protected final double fee;
    protected final String strategyName;
    protected TradeDetail tradeDetail;

    public abstract void process(Map<Long, Kline> qqqKlineMap, Map<Long, Kline> tqqqKlineMap, Map<Long, Kline> sqqqKlineMap);

    public AbstractQQQStrategy(double initialBalance, double fee, String strategyName) {
        this.initialBalance = initialBalance;
        this.fee = fee;
        this.strategyName = strategyName;
    }

    public void printTradeDetail() {
        List<Order> orders = tradeDetail.getOrders();
        for (Order order : orders) {
            long timestamp = order.getTimestamp();
            String date = DateUtils.getDate(timestamp);
            double price = order.getPrice();
            double quantity = order.getQuantity();
            String symbol = order.getSymbol();
            String type = order.getType();
            log.info("{}: {} {}, {} * {} = {}", date, type, symbol, price, quantity, price * quantity);
        }
        Workbook tradeDetailWorkbook = ExcelUtils.singleSheet(Collections.singletonList(tradeDetail), "order-list");
        ExcelUtils.exportWorkbook(tradeDetailWorkbook, "export/trade-detail-" + strategyName + ".xlsx");
        Workbook orderWorkbook = ExcelUtils.singleSheet(orders, "order-list");
        ExcelUtils.exportWorkbook(orderWorkbook, "export/orders-" + strategyName + ".xlsx");

        Map<String, ProfitResult> results = OrderCalculator.calculateProfitAndHoldingDays(orders);
        for (Map.Entry<String, ProfitResult> entry : results.entrySet()) {
            ProfitResult profitResult = entry.getValue();
            log.info("{}: holding days:{}, profit:{}", entry.getKey(), profitResult.getTotalHoldingDays(), profitResult.getProfit());
        }
        log.info("init balance:{}, finish balance:{}", tradeDetail.getInitialBalance(), tradeDetail.getBalance());
        log.info("fee:{}", tradeDetail.getFeeCount());
    }
}
