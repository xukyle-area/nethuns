package com.gantenx.nethuns.strategies;

import com.gantenx.nethuns.binance.model.OrderResponse;
import com.gantenx.nethuns.binance.service.BinanceService;
import com.gantenx.nethuns.commons.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RsiStrategy {

    public static void main(String[] args) {
        // Symbol symbol = Symbol.BTCUSDT;
        // String startStr = "20240101";
        // String endStr = "20241001";
        // long start = DateUtils.getTimestamp(startStr);
        // long end = DateUtils.getTimestamp(endStr);
        // Map<Long, Kline> klineMap = KlineService.getKLineMap(symbol, Period.D_1, start, end);
        // RsiIndicator rsiIndicator = new RsiIndicator(klineMap);
        // ExportUtils.saveJFreeChartAsImage(rsiIndicator.getChart(), "RSI");
        //
        // // 跌破 30
        // CrossedDownIndicatorRule buyRule = new CrossedDownIndicatorRule(rsiIndicator, 30.0d);
        // // 涨破 70
        // CrossedUpIndicatorRule sellRule = new CrossedUpIndicatorRule(rsiIndicator, 70.0d);
        // TradeExecutor tradeExecutor = new TradeExecutor(klineMap, symbol, buyRule, sellRule);
        // TradeExecutor.processAndExport(tradeExecutor);
        //
        // AccountInfo accountInfo = BinanceService.getAccountInfo();
        // log.info("{}", JsonUtils.toJson(accountInfo));

        // AccountInfo accountInfo = BinanceService.getAccountInfo();
        // log.info("{}", JsonUtils.toJson(accountInfo));
        OrderResponse orderResponse = BinanceService.orderTest();
        log.info("{}", JsonUtils.toJson(orderResponse));

    }
}
