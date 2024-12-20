package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Symbol.*;
@Slf4j
public class ImprovedRsiStrategy extends BaseStrategy {
    // RSI相关参数
    private static final double EXTREME_OVERSOLD = 25.0;
    private static final double EXTREME_OVERBOUGHT = 85.0;
    private static final double NEUTRAL_LEVEL = 60.0;
    private static final int RSI_PERIOD = 6;

    // 趋势确认参数
    private static final int TREND_PERIOD = 20;
    private static final double TREND_THRESHOLD = 0.02; // 2%变化确认趋势

    // 止损参数
    private static final double STOP_LOSS_THRESHOLD = 0.05; // 5%止损
    private static final double TRAILING_STOP = 0.08; // 8%追踪止损

    // 持仓时间限制
    private static final int MAX_LEVERAGED_HOLDING_DAYS = 5; // 最大杠杆持仓天数

    private Map<String, Integer> holdingDays = new HashMap<>();
    private Map<String, Double> maxPrices = new HashMap<>();

    public ImprovedRsiStrategy(String startStr, String endStr) {
        super(RsiStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        Map<Long, Double> rsiOfQQQ = IndexTechnicalIndicators.calculateRSI(qqqKlineMap, RSI_PERIOD);
        // 计算额外的技术指标
        Map<Long, Double> emaMap = IndexTechnicalIndicators.calculateEMA(qqqKlineMap, TREND_PERIOD);

        List<Long> timestamps = CollectionUtils.getTimestamps(rsiOfQQQ);

//        for (long ts : timestamps) {
//            if (!validateData(ts, rsiOfQQQ, emaMap)) {
//                continue;
//            }
//
//            TradingContext context = buildTradingContext(ts, rsiOfQQQ, emaMap);
//            this.dailyTrade(context);
//            updateTrackingData(context);
//        }
    }

    private void dailyTrade(TradingContext context) {
        // 1. 检查止损条件
        if (checkStopLoss(context)) {
            return;
        }

        // 2. 检查持仓时间限制
        if (checkHoldingPeriod(context)) {
            return;
        }

        // 3. 默认QQQ持仓
        if (tradeEngine.hasNoPosition()) {
            tradeEngine.buyAll(QQQ, context.getQqqPrice(), context.getTimestamp());
            return;
        }

        // 4. 极端情况处理
        if (handleExtremeConditions(context)) {
            return;
        }

//        // 5. 常规条件处理
//        handleNormalConditions(context);
    }

    private boolean checkStopLoss(TradingContext context) {
        if (tradeEngine.hasNoPosition()) {
            return false;
        }

//        String currentSymbol = tradeMocker.getCurrentPosition().getSymbol();
//        double currentPrice = getCurrentPrice(currentSymbol, context);
//        double entryPrice = tradeMocker.getCurrentPosition().getEntryPrice();
//
//        // 固定止损
//        double loss = (currentPrice - entryPrice) / entryPrice;
//        if (loss < -STOP_LOSS_THRESHOLD) {
//            exitToQQQ(context, "Stop loss triggered");
//            return true;
//        }
//
//        // 追踪止损
//        Double maxPrice = maxPrices.get(currentSymbol);
//        if (maxPrice != null) {
//            double drawdown = (currentPrice - maxPrice) / maxPrice;
//            if (drawdown < -TRAILING_STOP) {
//                exitToQQQ(context, "Trailing stop triggered");
//                return true;
//            }
//        }

        return false;
    }

    private boolean checkHoldingPeriod(TradingContext context) {
//        String currentSymbol = tradeMocker.getCurrentPosition().getSymbol();
//        if (TQQQ.equals(currentSymbol) || SQQQ.equals(currentSymbol)) {
//            Integer days = holdingDays.get(currentSymbol);
//            if (days != null && days >= MAX_LEVERAGED_HOLDING_DAYS) {
//                exitToQQQ(context, "Max holding period reached");
//                return true;
//            }
//        }
        return false;
    }

    private boolean handleExtremeConditions(TradingContext context) {
        double rsi = context.getRsi();

        // 超卖条件：RSI低 + 确认趋势反转
        if (rsi < EXTREME_OVERSOLD && confirmUptrendReversal(context)) {
            if (!tradeEngine.hasPosition(TQQQ)) {
                this.allinTQQQ(context.getTqqqPrice(), context.getQqqPrice(), context.getTimestamp());
                log.info("Extreme oversold with reversal confirmation: Switch to TQQQ at RSI {}", rsi);
            }
            return true;
        }

        // 超买条件：RSI高 + 确认趋势反转
//        if (rsi > EXTREME_OVERBOUGHT && confirmDowntrendReversal(context)) {
//            if (!tradeMocker.hasPosition(SQQQ)) {
//                this.allinSQQQ(context.getSqqqPrice(), context.getQqqPrice(), context.getTimestamp());
//                log.info("Extreme overbought with reversal confirmation: Switch to SQQQ at RSI {}", rsi);
//            }
//            return true;
//        }

        return false;
    }

    private boolean confirmUptrendReversal(TradingContext context) {
        // 1. 价格突破EMA
        boolean priceAboveEMA = context.getQqqPrice() > context.getEma();

//        // 2. 成交量确认
//        boolean volumeConfirmation = checkVolumeIncrease(context);
//
//        // 3. 连续上涨确认
//        boolean consecutiveGain = checkConsecutiveGain(context, 2);

//        return priceAboveEMA && (volumeConfirmation || consecutiveGain);
        return false;
    }

    private void updateTrackingData(TradingContext context) {
//        String currentSymbol = tradeMocker.getCurrentPosition().getSymbol();
//
//        // 更新持仓天数
//        holdingDays.merge(currentSymbol, 1, Integer::sum);
//
//        // 更新最高价
//        double currentPrice = getCurrentPrice(currentSymbol, context);
//        maxPrices.compute(currentSymbol, (k, v) -> v == null ? currentPrice : Math.max(v, currentPrice));
    }


    public void allinTQQQ(double tqqqPrice, double qqqPrice, long ts) {
        tradeEngine.sellAll(QQQ, qqqPrice, ts);
        tradeEngine.buyAll(TQQQ, tqqqPrice, ts);
    }

    public void alloutTQQQ(double tqqqPrice, double qqqPrice, long ts) {
        tradeEngine.sellAll(TQQQ, tqqqPrice, ts);
        tradeEngine.buyAll(QQQ, qqqPrice, ts);
    }

    public void allinSQQQ(double sqqqPrice, double qqqPrice, long ts) {
        tradeEngine.sellAll(QQQ, qqqPrice, ts);
        tradeEngine.buyAll(SQQQ, sqqqPrice, ts);
    }

    public void alloutSQQQ(double sqqqPrice, double qqqPrice, long ts) {
        tradeEngine.sellAll(SQQQ, sqqqPrice, ts);
        tradeEngine.buyAll(QQQ, qqqPrice, ts);
    }

    private void exitToQQQ(TradingContext context, String reason) {
//        if (tradeMocker.hasPosition(TQQQ)) {
//            alloutTQQQ(context.getTqqqPrice(), context.getQqqPrice(), context.getTimestamp());
//        } else if (tradeMocker.hasPosition(SQQQ)) {
//            alloutSQQQ(context.getSqqqPrice(), context.getQqqPrice(), context.getTimestamp());
//        }
//
//        // 清除跟踪数据
//        String symbol = tradeMocker.getCurrentPosition().getSymbol();
//        holdingDays.remove(symbol);
//        maxPrices.remove(symbol);
//
//        log.info("Exit to QQQ: {}", reason);
    }

    private static class TradingContext {
        private long timestamp;
        private double rsi;
        private double ema;
        private double qqqPrice;
        private double tqqqPrice;
        private double sqqqPrice;
        private double volume;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public double getRsi() {
            return rsi;
        }

        public void setRsi(double rsi) {
            this.rsi = rsi;
        }

        public double getEma() {
            return ema;
        }

        public void setEma(double ema) {
            this.ema = ema;
        }

        public double getQqqPrice() {
            return qqqPrice;
        }

        public void setQqqPrice(double qqqPrice) {
            this.qqqPrice = qqqPrice;
        }

        public double getTqqqPrice() {
            return tqqqPrice;
        }

        public void setTqqqPrice(double tqqqPrice) {
            this.tqqqPrice = tqqqPrice;
        }

        public double getSqqqPrice() {
            return sqqqPrice;
        }

        public void setSqqqPrice(double sqqqPrice) {
            this.sqqqPrice = sqqqPrice;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }
    }
}