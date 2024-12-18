package com.gantenx.strategy.qqq;

import com.gantenx.calculator.OrderCalculator;
import com.gantenx.model.*;
import com.gantenx.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class BaseStrategy {

    protected final double initialBalance;
    protected final double fee;
    protected final String strategyName;
    protected TradeDetail tradeDetail;

    public abstract void process(Map<Long, Kline> qqqKlineMap, Map<Long, Kline> tqqqKlineMap, Map<Long, Kline> sqqqKlineMap);

    public BaseStrategy(double initialBalance, double fee, String strategyName) {
        this.initialBalance = initialBalance;
        this.fee = fee;
        this.strategyName = strategyName;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public TradeDetail printTradeDetail() {
        List<Order> orders = tradeDetail.getOrders();
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
        log.info("fee: {}", tradeDetail.getFeeCount());
        return this.tradeDetail;
    }


}
