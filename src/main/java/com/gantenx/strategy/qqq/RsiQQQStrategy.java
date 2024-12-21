package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.chart.qqq.RSIChart;
import com.gantenx.engine.Position;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.PROPORTION_OF_100;
import static com.gantenx.constant.Constants.RSI_PERIOD;
import static com.gantenx.constant.QQQSymbol.*;

@Slf4j
public class RsiQQQStrategy extends BaseQQQStrategy {

    // RSI阈值
    private static final double EXTREME_OVERSOLD = 25.0;
    private static final double EXTREME_OVERBOUGHT = 85.0;
    private static final double NEUTRAL_LEVEL = 60.0;


    // 风险控制参数
    private static final double MAX_HOLDING_DAYS = 100.0; // 最大持仓天数（天）
    private static final double STOP_LOSS_THRESHOLD = -0.03; // 止损阈值（-5%）

    public RsiQQQStrategy(String startStr, String endStr) {
        super(RsiQQQStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        Map<Long, Double> rsiOfQQQ = IndexTechnicalIndicators.calculateRSI(qqqKlineMap, RSI_PERIOD);
        List<Long> timestamps = CollectionUtils.getTimestamps(rsiOfQQQ);

        for (long timestamp : timestamps) {
            Double rsi = rsiOfQQQ.get(timestamp);
            Kline tqqqCandle = tqqqKlineMap.get(timestamp);
            Kline qqqCandle = qqqKlineMap.get(timestamp);
            Kline sqqqCandle = sqqqKlineMap.get(timestamp);

            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle) || Objects.isNull(tqqqCandle)) {
                continue;
            }

            // 检查风险控制条件
            if (checkRiskControl(timestamp)) {
                continue;
            }

            this.dailyTrade(tqqqCandle.getClose(), qqqCandle.getClose(), sqqqCandle.getClose(), rsi, timestamp);
        }
    }

    private boolean checkRiskControl(long currentTime) {
        // 检查TQQQ持仓
        if (tradeEngine.hasPosition(TQQQ)) {
            List<Position> tqqqPositions = tradeEngine.getPositions(TQQQ);
            double avgHoldingDays = Position.getAverageHoldingDays(tqqqPositions, currentTime);
            double avgPrice = Position.getAveragePrice(tqqqPositions, currentTime);
            double currentPrice = tqqqKlineMap.get(currentTime).getClose();

            // 检查持仓时间
            if (avgHoldingDays >= MAX_HOLDING_DAYS) {
                exitToQQQ(currentTime, String.format("TQQQ平均持仓天数(%.2f)超过阈值", avgHoldingDays));
                return true;
            }

            // 检查止损
            double returnRate = (currentPrice - avgPrice) / avgPrice;
            if (returnRate <= STOP_LOSS_THRESHOLD) {
                exitToQQQ(currentTime, String.format("TQQQ跌幅(%.2f%%)触发止损", returnRate * 100));
                return true;
            }
        }

        // 检查SQQQ持仓
        if (tradeEngine.hasPosition(SQQQ)) {
            List<Position> sqqqPositions = tradeEngine.getPositions(SQQQ);
            double avgHoldingDays = Position.getAverageHoldingDays(sqqqPositions, currentTime);
            double avgPrice = Position.getAveragePrice(sqqqPositions, currentTime);
            double currentPrice = sqqqKlineMap.get(currentTime).getClose();

            // 检查持仓时间
            if (avgHoldingDays >= MAX_HOLDING_DAYS) {
                exitToQQQ(currentTime, String.format("SQQQ平均持仓天数(%.2f)超过阈值", avgHoldingDays));
                return true;
            }

            // 检查止损
            double returnRate = (currentPrice - avgPrice) / avgPrice;
            if (returnRate <= STOP_LOSS_THRESHOLD) {
                exitToQQQ(currentTime, String.format("SQQQ跌幅(%.2f%%)触发止损", returnRate * 100));
                return true;
            }
        }

        return false;
    }

    private void exitToQQQ(long timestamp, String reason) {
        double qqqPrice = qqqKlineMap.get(timestamp).getClose();
        double tqqqPrice = tqqqKlineMap.get(timestamp).getClose();
        double sqqqPrice = sqqqKlineMap.get(timestamp).getClose();

        // 清空TQQQ持仓
        if (tradeEngine.hasPosition(TQQQ)) {
            tradeEngine.sell(TQQQ, tqqqPrice, PROPORTION_OF_100, timestamp, reason);
        }

        // 清空SQQQ持仓
        if (tradeEngine.hasPosition(SQQQ)) {
            tradeEngine.sell(SQQQ, sqqqPrice, PROPORTION_OF_100, timestamp, reason);
        }

        // 买入QQQ
        tradeEngine.buy(QQQ, qqqPrice, PROPORTION_OF_100, timestamp, reason + "，转入QQQ");
    }

    private void dailyTrade(double tqqqPrice, double qqqPrice, double sqqqPrice, double rsi, long timestamp) {
        // 没有仓位的时候，持有QQQ
        if (tradeEngine.hasNoPosition()) {
            tradeEngine.buy(QQQ, qqqPrice, PROPORTION_OF_100, timestamp, "无持仓，买入QQQ");
            return;
        }

        // RSI超卖，买入TQQQ
        if (rsi < EXTREME_OVERSOLD) {
            tradeEngine.sell(QQQ, qqqPrice, PROPORTION_OF_100, timestamp,
                             String.format("RSI=%.2f 超卖，卖出QQQ换入TQQQ", rsi));
            tradeEngine.buy(TQQQ, tqqqPrice, PROPORTION_OF_100, timestamp,
                            String.format("RSI=%.2f 超卖，买入TQQQ", rsi));
            return;
        }
        // RSI超买，买入SQQQ
        else if (rsi > EXTREME_OVERBOUGHT) {
            tradeEngine.sell(QQQ, qqqPrice, PROPORTION_OF_100, timestamp,
                             String.format("RSI=%.2f 超买，卖出QQQ换入SQQQ", rsi));
            tradeEngine.buy(SQQQ, sqqqPrice, PROPORTION_OF_100, timestamp,
                            String.format("RSI=%.2f 超买，买入SQQQ", rsi));
            return;
        }

        // 处理正常持仓情况
        handleNormalHolding(tqqqPrice, qqqPrice, sqqqPrice, rsi, timestamp);
    }

    private void handleNormalHolding(double tqqqPrice, double qqqPrice, double sqqqPrice, double rsi, long timestamp) {
        // 持有SQQQ且RSI回归中性，换回QQQ
        if (tradeEngine.hasPosition(SQQQ) && rsi <= NEUTRAL_LEVEL) {
            tradeEngine.sell(SQQQ, sqqqPrice, PROPORTION_OF_100, timestamp,
                             String.format("持有SQQQ，RSI=%.2f 回归中性，卖出SQQQ换回QQQ", rsi));
            tradeEngine.buy(QQQ, qqqPrice, PROPORTION_OF_100, timestamp,
                            String.format("RSI=%.2f 回归中性，买入QQQ", rsi));
        }
        // 持有TQQQ且RSI回归中性，换回QQQ
        else if (tradeEngine.hasPosition(TQQQ) && rsi >= NEUTRAL_LEVEL) {
            tradeEngine.sell(TQQQ, tqqqPrice, PROPORTION_OF_100, timestamp,
                             String.format("持有TQQQ，RSI=%.2f 回归中性，卖出TQQQ换回QQQ", rsi));
            tradeEngine.buy(QQQ, qqqPrice, PROPORTION_OF_100, timestamp,
                            String.format("RSI=%.2f 回归中性，买入QQQ", rsi));
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        RSIChart chart = new RSIChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        return chart.getCombinedChart();
    }
}