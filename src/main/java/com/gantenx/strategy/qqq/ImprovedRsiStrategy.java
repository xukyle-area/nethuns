package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.constant.Symbol;
import com.gantenx.engine.TradingContext;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.constant.Constants.MACD;
import static com.gantenx.constant.Symbol.*;
import static com.gantenx.strategy.qqq.ImprovedRsiStrategyTL.*;

@Slf4j
public class ImprovedRsiStrategy extends BaseStrategy {

    private long lastTradeTime = 0;
    private Map<Long, TradingContext> contextCache;

    public ImprovedRsiStrategy(String startStr, String endStr) {
        super(ImprovedRsiStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        this.initCache();
        contextCache.keySet().stream().sorted().forEach(ts -> {
            TradingContext context = contextCache.get(ts);
            if (!executeRiskManagement(context)) {
                executeTradingLogic(context);
            }
        });
    }

    double prevRsi;

    private void initCache() {
        Map<Long, Double> rsiMap = IndexTechnicalIndicators.calculateRSI(qqqKlineMap, RSI_PERIOD);
        Map<Long, Double> emaMap = IndexTechnicalIndicators.calculateEMA(qqqKlineMap, TREND_PERIOD);
        Map<Long, Double> fastEmaMap = IndexTechnicalIndicators.calculateEMA(qqqKlineMap, FAST_EMA_PERIOD);
        Map<String, Map<Long, Double>> macdData = IndexTechnicalIndicators.calculateMACDWithSignal(qqqKlineMap);

        contextCache = new HashMap<>();
        Map<Long, Double> macdMap = macdData.get(MACD);
        CollectionUtils.getTimestamps(rsiMap).stream().filter(ts -> validateData(ts,
                                                                                 rsiMap,
                                                                                 emaMap,
                                                                                 fastEmaMap,
                                                                                 macdMap)).forEach(timestamp -> {
            Double rsi = rsiMap.get(timestamp);
            double qqqPrice = qqqKlineMap.get(timestamp).getClose();
            double tqqqPrice = tqqqKlineMap.get(timestamp).getClose();
            double sqqqPrice = sqqqKlineMap.get(timestamp).getClose();
            Double ema = emaMap.get(timestamp);
            Double fastEma = fastEmaMap.get(timestamp);
            Double macd = macdMap.get(timestamp);
            ConsecutiveDays consecutiveDays = ConsecutiveDays.calculateConsecutiveDays(timestamp, qqqKlineMap);
            TradingContext context = TradingContext.buildTradingContext(timestamp,
                                                                        rsi,
                                                                        qqqPrice,
                                                                        tqqqPrice,
                                                                        sqqqPrice,
                                                                        ema,
                                                                        fastEma,
                                                                        macd,
                                                                        prevRsi,
                                                                        consecutiveDays);
            prevRsi = rsi;
            contextCache.put(timestamp, context);
        });
    }

    @SafeVarargs
    private final boolean validateData(long ts, Map<Long, Double>... indicators) {
        for (Map<Long, Double> indicator : indicators) {
            if (!indicator.containsKey(ts)) {
                return false;
            }
        }
        return qqqKlineMap.containsKey(ts) && tqqqKlineMap.containsKey(ts) && sqqqKlineMap.containsKey(ts);
    }


    private boolean executeRiskManagement(TradingContext context) {
        return this.checkStopLoss(context) || this.checkHoldingPeriod(context);
    }

    private boolean checkHoldingPeriod(TradingContext context) {
        return tradeEngine.getPositions().entrySet().stream().anyMatch(entry -> {
            if (context.getTimestamp() - entry.getValue().get(0).getTimestamp() >= MAX_LEVERAGED_HOLDING_DAYS) {
                this.exitToQQQ(context, "Max holding period reached for " + entry.getKey());
                return true;
            }
            return false;
        });
    }

    private boolean checkStopLoss(TradingContext context) {
        return tradeEngine.getPositions().entrySet().stream().anyMatch(entry -> {
            Symbol symbol = entry.getKey();
            double currentPrice = getCurrentPrice(symbol, context);
            double loss = (currentPrice - entry.getValue().get(0).getPrice()) / entry.getValue().get(0).getPrice();

            if (loss < -STOP_LOSS_THRESHOLD) {
                this.exitToQQQ(context, "Stop loss triggered");
                return true;
            }
            return false;
        });
    }

    private void executeTradingLogic(TradingContext context) {
        if (tradeEngine.getQuantity(QQQ) == 0 && tradeEngine.getQuantity(TQQQ) == 0 && tradeEngine.getQuantity(SQQQ) == 0) {
            tradeEngine.buyAll(QQQ, context.getQqqPrice(), context.getTimestamp());
            this.updateLastTradeTime(context.getTimestamp());
        } else if (this.canTrade(context)) {
            this.handleTradeSignals(context);
        }
    }

    private boolean canTrade(TradingContext context) {
        if (context.getTimestamp() - lastTradeTime < MIN_TRADE_INTERVAL) {
            return false;
        }

        double volatility = this.calculateDailyVolatility(context);
        if (volatility > MAX_VOLATILITY_THRESHOLD) {
            log.info("Market volatility too high: {}", volatility);
            return false;
        }

        return isPositionAllowed(context);
    }

    private boolean isPositionAllowed(TradingContext context) {

        return true;
    }

    private double calculateDailyVolatility(TradingContext context) {
        // 简单实现：用当日高低价差计算波动率
        double high = qqqKlineMap.get(context.getTimestamp()).getHigh();
        double low = qqqKlineMap.get(context.getTimestamp()).getLow();
        return (high - low) / low;
    }


    private void handleTradeSignals(TradingContext context) {
        if (this.isExtremeBuySignal(context)) {
            this.executeExtremeBuyTrade(context);
        } else if (this.isExtremeSellSignal(context)) {
            this.executeExtremeSellTrade(context);
        } else {
            this.handleNeutralConditions(context);
        }
    }

    private boolean isExtremeBuySignal(TradingContext context) {
        return context.getRsi() < EXTREME_OVERSOLD && TradingContext.confirmUptrendReversal(context);
    }

    private boolean isExtremeSellSignal(TradingContext context) {
        return context.getRsi() > EXTREME_OVERBOUGHT && TradingContext.confirmDowntrendReversal(context);
    }

    private void executeExtremeBuyTrade(TradingContext context) {
        if (tradeEngine.getQuantity(TQQQ) == 0) {
            tradeEngine.buyAll(TQQQ, context.getTqqqPrice(), context.getTimestamp());
            tradeEngine.sellAll(QQQ, context.getQqqPrice(), context.getTimestamp());
            updateLastTradeTime(context.getTimestamp());
        }
    }

    private void executeExtremeSellTrade(TradingContext context) {
        if (tradeEngine.getQuantity(SQQQ) == 0) {
            tradeEngine.buyAll(SQQQ, context.getSqqqPrice(), context.getTimestamp());
            tradeEngine.sellAll(QQQ, context.getQqqPrice(), context.getTimestamp());
            updateLastTradeTime(context.getTimestamp());
        }
    }

    private void updateLastTradeTime(long timestamp) {
        lastTradeTime = timestamp;
    }

    private void handleNeutralConditions(TradingContext context) {
        double rsi = context.getRsi();
        if (rsi >= EXTREME_OVERSOLD && rsi <= EXTREME_OVERBOUGHT && tradeEngine.getQuantity(QQQ) == 0) {
            tradeEngine.buyAll(QQQ, context.getQqqPrice(), context.getTimestamp());
        }
    }

    private void exitToQQQ(TradingContext context, String reason) {
        tradeEngine.sellAll(TQQQ, context.getTqqqPrice(), context.getTimestamp());
        tradeEngine.sellAll(SQQQ, context.getSqqqPrice(), context.getTimestamp());
        tradeEngine.buyAll(QQQ, context.getQqqPrice(), context.getTimestamp());
        log.info("Exit to QQQ: {}", reason);
    }

    private double getCurrentPrice(Symbol symbol, TradingContext context) {
        switch (symbol) {
            case QQQ:
                return context.getQqqPrice();
            case TQQQ:
                return context.getTqqqPrice();
            case SQQQ:
                return context.getSqqqPrice();
            default:
                throw new IllegalArgumentException("Unknown symbol: " + symbol);
        }
    }
}
