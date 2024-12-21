package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.chart.qqq.RSIChart;
import com.gantenx.constant.QQQSymbol;
import com.gantenx.engine.Position;
import com.gantenx.engine.TradingContext;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.*;

import static com.gantenx.constant.Constants.MACD;
import static com.gantenx.constant.Constants.PROPORTION_OF_100;
import static com.gantenx.constant.QQQSymbol.*;


@Slf4j
public class ImprovedRsiQQQStrategy extends BaseQQQStrategy {

    private static final double EXTREME_OVERSOLD = 25.0;
    private static final double EXTREME_OVERBOUGHT = 85.0;
    private static final int RSI_PERIOD = 6;
    private static final int TREND_PERIOD = 20;
    private static final int FAST_EMA_PERIOD = 5;
    private static final double STOP_LOSS_THRESHOLD = 0.05;
    private static final int MAX_LEVERAGED_HOLDING_DAYS = 5 * 24 * 3600 * 1000;
    private static final long MIN_TRADE_INTERVAL = 24 * 60 * 60 * 1000;
    private static final double MAX_VOLATILITY_THRESHOLD = 0.03;

    private long lastTradeTime = 0;
    private Map<Long, TradingContext> contextCache;

    public ImprovedRsiQQQStrategy(String startStr, String endStr) {
        super(ImprovedRsiQQQStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        this.initCache();
        List<Long> longList = CollectionUtils.getTimestamps(contextCache);
        for (Long timestamp : longList) {
            TradingContext context = contextCache.get(timestamp);
            this.executeRiskManagement(context);
            this.executeTradingLogic(context);
        }
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

    private void executeRiskManagement(TradingContext context) {
        long currentTimestamp = context.getTimestamp();
        Map<QQQSymbol, List<Position>> positionMap = tradeEngine.getPositions();
        for (Map.Entry<QQQSymbol, List<Position>> entry : positionMap.entrySet()) {
            QQQSymbol QQQSymbol = entry.getKey();
            List<Position> positions = entry.getValue();
            double averagePrice = Position.getAveragePrice(positions, currentTimestamp);
            double averageHoldingDays = Position.getAverageHoldingDays(positions, currentTimestamp);
            double currentPrice = this.getCurrentPrice(QQQSymbol, context);
            if (QQQSymbol.equals(QQQ)) {
                continue;
            }
            if (averageHoldingDays >= MAX_LEVERAGED_HOLDING_DAYS) {
                tradeEngine.sell(QQQSymbol, currentPrice, PROPORTION_OF_100, currentTimestamp, "超过持有天数，卖出");
            }
            double lossRate = (currentPrice - averagePrice) / averagePrice;
            if (lossRate < -STOP_LOSS_THRESHOLD) {
                tradeEngine.sell(QQQSymbol,
                                 currentPrice,
                                 PROPORTION_OF_100,
                                 currentTimestamp,
                                 "损失达到：" + lossRate + "，止损点已达到");
            }
        }
    }

    private void executeTradingLogic(TradingContext context) {
        if (tradeEngine.getQuantity(QQQ) == 0 && tradeEngine.getQuantity(TQQQ) == 0 && tradeEngine.getQuantity(SQQQ) == 0) {
            tradeEngine.buy(QQQ, context.getQqqPrice(), PROPORTION_OF_100, context.getTimestamp(), "没有持仓，买入 QQQ");
            lastTradeTime = context.getTimestamp();
        } else if (this.canTrade(context)) {
            this.handleTradeSignals(context);
        }
    }

    private boolean canTrade(TradingContext context) {
        // 最低交易间隔为 1 天
        if (context.getTimestamp() - lastTradeTime < MIN_TRADE_INTERVAL) {
            return false;
        }

        double volatility = this.calculateDailyVolatility(context);
        // 如果当天价格波动太大，不允许交易
        if (volatility > MAX_VOLATILITY_THRESHOLD) {
            log.info("Market volatility too high: {}", volatility);
            return false;
        }

        return true;
    }

    private double calculateDailyVolatility(TradingContext context) {
        // 简单实现：用当日高低价差计算波动率
        long timestamp = context.getTimestamp();
        double high = qqqKlineMap.get(timestamp).getHigh();
        double low = qqqKlineMap.get(timestamp).getLow();
        return (high - low) / low;
    }

    private void handleTradeSignals(TradingContext context) {
        if (this.isExtremeBuySignal(context)) {
            this.allinBuy(context, TQQQ, "");
        } else if (this.isExtremeSellSignal(context)) {
            this.allinBuy(context, SQQQ, "");
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

    private void allinBuy(TradingContext context, QQQSymbol QQQSymbol, String reason) {
        if (QQQSymbol.equals(QQQ)) {
            return;
        }
        tradeEngine.sell(QQQ,
                         context.getQqqPrice(),
                         PROPORTION_OF_100,
                         context.getTimestamp(),
                         "出现了极端的上涨或者下跌趋势，卖出QQQ，买入");
        tradeEngine.buy(QQQSymbol, context.getTqqqPrice(), PROPORTION_OF_100, context.getTimestamp(), reason);
        lastTradeTime = context.getTimestamp();
    }

    private void handleNeutralConditions(TradingContext context) {
        double rsi = context.getRsi();

        if (rsi >= EXTREME_OVERSOLD && tradeEngine.getQuantity(TQQQ) > 0) {
            tradeEngine.sell(TQQQ,
                             context.getTqqqPrice(),
                             PROPORTION_OF_100,
                             context.getTimestamp(),
                             "RSI达到 " + rsi + ": 原先持仓TQQQ，目前判断已经上涨，卖出TQQQ，置换到QQQ");
            tradeEngine.buy(QQQ,
                            context.getQqqPrice(),
                            PROPORTION_OF_100,
                            context.getTimestamp(),
                            "RSI达到 " + rsi + ": 原先持仓TQQQ，目前判断已经上涨，买入QQQ");
        }

        tradeEngine.sell(SQQQ,
                         context.getSqqqPrice(),
                         PROPORTION_OF_100,
                         context.getTimestamp(),
                         "RSI达到 " + rsi + ": 原先持仓SQQQ，目前判断已经下跌，卖出SQQQ，置换到QQQ");
        tradeEngine.buy(QQQ,
                        context.getQqqPrice(),
                        PROPORTION_OF_100,
                        context.getTimestamp(),
                        "RSI达到 " + rsi + ": 原先持仓SQQQ，目前判断已经下跌，买入QQQ");
    }

    private double getCurrentPrice(QQQSymbol QQQSymbol, TradingContext context) {
        switch (QQQSymbol) {
            case QQQ:
                return context.getQqqPrice();
            case TQQQ:
                return context.getTqqqPrice();
            case SQQQ:
                return context.getSqqqPrice();
            default:
                throw new IllegalArgumentException("Unknown symbol: " + QQQSymbol);
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        RSIChart chart = new RSIChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        return chart.getCombinedChart();
    }
}
