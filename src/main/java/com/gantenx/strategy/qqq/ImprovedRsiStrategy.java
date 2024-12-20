package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.constant.Symbol;
import com.gantenx.engine.Position;
import com.gantenx.engine.TradingContext;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.constant.Symbol.*;
import static com.gantenx.strategy.qqq.ImprovedRsiStrategyTL.*;

@Slf4j
public class ImprovedRsiStrategy extends BaseStrategy {
    private final EnumMap<Symbol, Integer> holdingDays = new EnumMap<>(Symbol.class);
    private final EnumMap<Symbol, Double> maxPrices = new EnumMap<>(Symbol.class);
    private long lastTradeTime = 0;

    // 缓存技术指标
    private Map<Long, TradingContext> contextCache;

    public ImprovedRsiStrategy(String startStr, String endStr) {
        super(ImprovedRsiStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        this.calculateIndicators();
        List<Long> timestamps = new ArrayList<>(contextCache.keySet());
        Collections.sort(timestamps);

        for (long ts : timestamps) {
            TradingContext context = contextCache.get(ts);
            executeTradingLogic(context);
            updateTrackingData(context);
        }
    }

    private void calculateIndicators() {
        Map<Long, Double> rsiMap = IndexTechnicalIndicators.calculateRSI(qqqKlineMap, ImprovedRsiStrategyTL.RSI_PERIOD);
        Map<Long, Double> emaMap = IndexTechnicalIndicators.calculateEMA(qqqKlineMap, ImprovedRsiStrategyTL.TREND_PERIOD);
        Map<Long, Double> fastEmaMap = IndexTechnicalIndicators.calculateEMA(qqqKlineMap, ImprovedRsiStrategyTL.FAST_EMA_PERIOD);
        Map<String, Map<Long, Double>> macdData = IndexTechnicalIndicators.calculateMACDWithSignal(qqqKlineMap);

        contextCache = new HashMap<>();

        List<Long> timestamps = CollectionUtils.getTimestamps(rsiMap);
        Double previousRsi = null;
        for (long ts : timestamps) {
            if (validateData(ts, rsiMap, emaMap, fastEmaMap, macdData.get("macd"))) {
                TradingContext context = buildTradingContext(ts, rsiMap, emaMap, fastEmaMap, macdData.get("macd"), previousRsi);
                contextCache.put(ts, context);
                previousRsi = context.getRsi();
            }
        }
    }

    private TradingContext buildTradingContext(long ts, Map<Long, Double> rsiMap, Map<Long, Double> emaMap, Map<Long, Double> fastEmaMap, Map<Long, Double> macd, Double previousRsi) {
        ConsecutiveDays consecutiveDays = calculateConsecutiveDays(ts);

        TradingContext context = new TradingContext();
        context.setTimestamp(ts);
        context.setRsi(rsiMap.get(ts));
        context.setEma(emaMap.get(ts));
        context.setFastEma(fastEmaMap.get(ts));
        context.setMacd(macd.get(ts));
        context.setQqqPrice(qqqKlineMap.get(ts).getClose());
        context.setTqqqPrice(tqqqKlineMap.get(ts).getClose());
        context.setSqqqPrice(sqqqKlineMap.get(ts).getClose());
        context.setConsecutiveGainDays(consecutiveDays.getGainDays());
        context.setConsecutiveLossDays(consecutiveDays.getLossDays());
        context.setPreviousRsi(previousRsi);

        return context;
    }

    private void executeTradingLogic(TradingContext context) {
        // 风险检查
        if (checkRiskManagement(context)) {
            return;
        }

        if (needDefaultPosition()) {
            establishDefaultPosition(context);
            return;
        }

        if (canTrade(context)) {
            handleTradeSignals(context);
        }
    }

    private boolean checkRiskManagement(TradingContext context) {
        return checkStopLoss(context) || checkHoldingPeriod(context);
    }

    private boolean needDefaultPosition() {
        return tradeEngine.getQuantity(QQQ) == 0 && tradeEngine.getQuantity(TQQQ) == 0 && tradeEngine.getQuantity(SQQQ) == 0;
    }

    private void establishDefaultPosition(TradingContext context) {
        tradeEngine.buyAll(QQQ, context.getQqqPrice(), context.getTimestamp());
        updateLastTradeTime(context.getTimestamp());
    }

    private void handleTradeSignals(TradingContext context) {
        double rsi = context.getRsi();

        if (isExtremeBuySignal(context)) {
            executeExtremeBuyTrade(context);
        } else if (isExtremeSellSignal(context)) {
            executeExtremeSellTrade(context);
        } else {
            handleNormalConditions(context);
        }
    }

    private boolean isExtremeBuySignal(TradingContext context) {
        return context.getRsi() < ImprovedRsiStrategyTL.EXTREME_OVERSOLD && TradingContext.confirmUptrendReversal(context);
    }

    private boolean isExtremeSellSignal(TradingContext context) {
        return context.getRsi() > EXTREME_OVERBOUGHT && TradingContext.confirmDowntrendReversal(context);
    }

    private void executeExtremeBuyTrade(TradingContext context) {
        if (tradeEngine.getQuantity(TQQQ) == 0) {
            allinTQQQ(context.getTqqqPrice(), context.getQqqPrice(), context.getTimestamp());
            updateLastTradeTime(context.getTimestamp());
        }
    }

    private void executeExtremeSellTrade(TradingContext context) {
        if (tradeEngine.getQuantity(SQQQ) == 0) {
            allinSQQQ(context.getSqqqPrice(), context.getQqqPrice(), context.getTimestamp());
            updateLastTradeTime(context.getTimestamp());
        }
    }

    private void updateLastTradeTime(long timestamp) {
        lastTradeTime = timestamp;
        log.debug("Updated last trade time to {}", timestamp);
    }

    public void allinSQQQ(double sqqqPrice, double qqqPrice, long ts) {
        tradeEngine.buyAll(SQQQ, sqqqPrice, ts);
        tradeEngine.sellAll(QQQ, qqqPrice, ts);
    }

    public void allinTQQQ(double tqqqPrice, double qqqPrice, long ts) {
        tradeEngine.buyAll(TQQQ, tqqqPrice, ts);
        tradeEngine.sellAll(QQQ, qqqPrice, ts);
    }

    private boolean checkHoldingPeriod(TradingContext context) {
        Map<Symbol, List<Position>> positions = tradeEngine.getPositions();

        for (Map.Entry<Symbol, List<Position>> entry : positions.entrySet()) {
            List<Position> positionList = entry.getValue();
            for (Position position : positionList) {
                if (context.getTimestamp() - position.getTimestamp() >= MAX_LEVERAGED_HOLDING_DAYS) {
                    exitToQQQ(context, "Max holding period reached for " + entry.getKey());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkStopLoss(TradingContext context) {
        if (tradeEngine.getQuantity(QQQ) == 0 && tradeEngine.getQuantity(TQQQ) == 0 && tradeEngine.getQuantity(SQQQ) == 0) {
            return false;
        }
        Map<Symbol, List<Position>> positions = tradeEngine.getPositions();
        for (Map.Entry<Symbol, List<Position>> entry : positions.entrySet()) {
            List<Position> positionList = entry.getValue();
            Symbol symbol = entry.getKey();
            for (Position position : positionList) {
                double currentPrice = getCurrentPrice(symbol, context);
                double price = position.getPrice();
                // 固定止损
                double loss = (currentPrice - price) / price;
                if (loss < -STOP_LOSS_THRESHOLD) {
                    exitToQQQ(context, "Stop loss triggered");
                    return true;
                }
                // 追踪止损
                Double maxPrice = maxPrices.get(symbol);
                if (maxPrice != null) {
                    double drawdown = (currentPrice - maxPrice) / maxPrice;
                    if (drawdown < -TRAILING_STOP) {
                        exitToQQQ(context, "Trailing stop triggered");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ConsecutiveDays calculateConsecutiveDays(long timestamp) {
        ConsecutiveDays result = new ConsecutiveDays();

        // 获取当前和前一个交易日的收盘价
        Kline currentKline = qqqKlineMap.get(timestamp);
        if (currentKline == null) {
            return result;
        }

        // 获取前一个交易日的数据
        Long previousTimestamp = findPreviousTimestamp(timestamp);
        if (previousTimestamp == null) {
            return result;
        }

        Kline previousKline = qqqKlineMap.get(previousTimestamp);
        if (previousKline == null) {
            return result;
        }

        // 计算连续涨跌天数
        double currentClose = currentKline.getClose();
        double previousClose = previousKline.getClose();

        if (currentClose > previousClose) {
            result.gainDays = countConsecutiveDays(timestamp, true);
            result.lossDays = 0;
        } else if (currentClose < previousClose) {
            result.gainDays = 0;
            result.lossDays = countConsecutiveDays(timestamp, false);
        }

        return result;
    }

    private Long findPreviousTimestamp(long currentTimestamp) {
        return qqqKlineMap.keySet().stream().filter(ts -> ts < currentTimestamp).max(Long::compareTo).orElse(null);
    }

    private int countConsecutiveDays(long timestamp, boolean isGain) {
        int count = 1; // 包含当天
        Long currentTs = timestamp;

        while (true) {
            Long previousTs = findPreviousTimestamp(currentTs);
            if (previousTs == null) {
                break;
            }

            Kline currentKline = qqqKlineMap.get(currentTs);
            Kline previousKline = qqqKlineMap.get(previousTs);

            boolean continues = isGain ? currentKline.getClose() > previousKline.getClose() : currentKline.getClose() < previousKline.getClose();

            if (!continues) {
                break;
            }

            count++;
            currentTs = previousTs;
        }

        return count;
    }


    private static class ConsecutiveDays {
        private int gainDays;
        private int lossDays;

        public ConsecutiveDays() {
            this.gainDays = 0;
            this.lossDays = 0;
        }

        public int getGainDays() {
            return gainDays;
        }

        public void setGainDays(int gainDays) {
            this.gainDays = gainDays;
        }

        public int getLossDays() {
            return lossDays;
        }

        public void setLossDays(int lossDays) {
            this.lossDays = lossDays;
        }
    }

    private void exitToQQQ(TradingContext context, String reason) {

        tradeEngine.sellAll(TQQQ, context.getTqqqPrice(), context.getTimestamp());
        tradeEngine.sellAll(SQQQ, context.getSqqqPrice(), context.getTimestamp());
        tradeEngine.buyAll(QQQ, context.getQqqPrice(), context.getTimestamp());

        log.info("Exit to QQQ: {}", reason);
    }

    private void updateTrackingData(TradingContext context) {
        Symbol currentSymbol = getCurrentSymbol();
        holdingDays.merge(currentSymbol, 1, Integer::sum);

        double currentPrice = getCurrentPrice(currentSymbol, context);
        maxPrices.compute(currentSymbol, (k, v) -> v == null ? currentPrice : Math.max(v, currentPrice));
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

    private boolean validateData(long ts, Map<Long, Double>... indicators) {
        for (Map<Long, Double> indicator : indicators) {
            if (!indicator.containsKey(ts)) {
                return false;
            }
        }
        return qqqKlineMap.containsKey(ts) && tqqqKlineMap.containsKey(ts) && sqqqKlineMap.containsKey(ts);
    }

    private void handleNormalConditions(TradingContext context) {
        double rsi = context.getRsi();

        // 在中性区域，保持QQQ持仓
        if (rsi >= EXTREME_OVERSOLD && rsi <= EXTREME_OVERBOUGHT) {
            if (tradeEngine.getQuantity(QQQ) == 0) {
                tradeEngine.buyAll(QQQ, context.getQqqPrice(), context.getTimestamp());
            }
            return;
        }

        // RSI低于超卖线，考虑买入TQQQ
        if (rsi < EXTREME_OVERSOLD && TradingContext.confirmUptrendReversal(context)) {
            if (tradeEngine.getQuantity(TQQQ) == 0 && canTrade(context)) {
                tradeEngine.buyAll(TQQQ, context.getTqqqPrice(), context.getTimestamp());
            }
        }
        // RSI高于超买线，考虑买入SQQQ
        else if (rsi > EXTREME_OVERBOUGHT && TradingContext.confirmDowntrendReversal(context)) {
            if (tradeEngine.getQuantity(SQQQ) == 0 && canTrade(context)) {
                tradeEngine.buyAll(SQQQ, context.getSqqqPrice(), context.getTimestamp());
            }
        }
    }

    private boolean canTrade(TradingContext context) {
        // 1. 检查交易间隔
        if (context.getTimestamp() - lastTradeTime < MIN_TRADE_INTERVAL) {
            return false;
        }

        // 2. 检查市场波动性
        double volatility = calculateDailyVolatility(context);
        if (volatility > MAX_VOLATILITY_THRESHOLD) {
            log.info("Market volatility too high: {}", volatility);
            return false;
        }

        // 3. 检查交易量是否足够
        if (!hasEnoughVolume(context)) {
            return false;
        }

        // 4. 检查交易成本
        if (!TradingContext.isTradeWorthwhile(context)) {
            return false;
        }

        // 5. 检查当前持仓状态
        if (!isPositionAllowed(context)) {
            return false;
        }

        return true;
    }

    private boolean hasEnoughVolume(TradingContext context) {
        double avgVolume = calculateAverageVolume(context);
        double currentVolume = qqqKlineMap.get(context.getTimestamp()).getVolume();
        return currentVolume >= avgVolume * 0.5; // 当日成交量至少为平均值的50%
    }

    private double calculateAverageVolume(TradingContext context) {
        // 计算过去N天的平均成交量
        final int VOLUME_DAYS = 20;
        double totalVolume = 0;
        int count = 0;

        long currentTime = context.getTimestamp();
        for (Map.Entry<Long, Kline> entry : qqqKlineMap.entrySet()) {
            if (entry.getKey() < currentTime && count < VOLUME_DAYS) {
                totalVolume += entry.getValue().getVolume();
                count++;
            }
        }

        return count > 0 ? totalVolume / count : 0;
    }


    private boolean isPositionAllowed(TradingContext context) {
        // 检查是否允许建立新仓位
        Symbol currentSymbol = getCurrentSymbol();

        // 如果当前持有杠杆产品且持仓时间过长,不允许新交易
        if ((currentSymbol == TQQQ || currentSymbol == SQQQ) && holdingDays.getOrDefault(currentSymbol, 0) >= MAX_LEVERAGED_HOLDING_DAYS) {
            return false;
        }

        // 根据RSI值检查是否适合建仓
        double rsi = context.getRsi();
        if (rsi < EXTREME_OVERSOLD || rsi > EXTREME_OVERBOUGHT) {
            // 在极端区域,需要额外确认
            return TradingContext.confirmUptrendReversal(context) || TradingContext.confirmDowntrendReversal(context);
        }

        return true;
    }

    private Symbol getCurrentSymbol() {
        Map<Symbol, List<Position>> positions = tradeEngine.getPositions();
        for (Map.Entry<Symbol, List<Position>> entry : positions.entrySet()) {
            if (!entry.getValue().isEmpty() && entry.getValue().get(0).getQuantity() > 0) {
                return entry.getKey();
            }
        }
        return QQQ;
    }

    private double calculateDailyVolatility(TradingContext context) {
        // 简单实现：用当日高低价差计算波动率
        double high = qqqKlineMap.get(context.getTimestamp()).getHigh();
        double low = qqqKlineMap.get(context.getTimestamp()).getLow();
        return (high - low) / low;
    }
}