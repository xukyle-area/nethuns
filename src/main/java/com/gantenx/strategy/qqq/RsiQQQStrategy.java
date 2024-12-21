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
    private static final double EXTREME_OVERSOLD = 25.0;
    private static final double EXTREME_OVERBOUGHT = 85.0;
    private static final double NEUTRAL_LEVEL = 60.0;
    private static final double MAX_HOLDING_DAYS = 100.0;
    private static final double STOP_LOSS_THRESHOLD = -0.03;

    public RsiQQQStrategy(String startStr, String endStr) {
        super(RsiQQQStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        Map<Long, Double> rsiOfQQQ = IndexTechnicalIndicators.calculateRSI(klineMap.get(QQQ), RSI_PERIOD);
        while (tradeEngine.hasNextDay()) {
            long timestamp = tradeEngine.nextDay();
            Double rsi = rsiOfQQQ.get(timestamp);
            if (Objects.isNull(rsi)) {
                continue;
            }
            if (checkRiskControl()) {
                continue;
            }
            this.dailyTrade(rsi);
        }
    }

    private boolean checkRiskControl() {
        long currentTime = tradeEngine.getTimestamp();
        // 检查TQQQ持仓
        if (tradeEngine.getQuantity(TQQQ) > 0) {
            List<Position> tqqqPositions = tradeEngine.getPositions(TQQQ);
            double avgHoldingDays = Position.getAverageHoldingDays(tqqqPositions, currentTime);
            double avgPrice = Position.getAveragePrice(tqqqPositions, currentTime);
            double currentPrice = tradeEngine.getPrice(TQQQ);

            if (avgHoldingDays >= MAX_HOLDING_DAYS) {
                exitToQQQ(String.format("TQQQ平均持仓天数(%.2f)超过阈值", avgHoldingDays));
                return true;
            }

            // 检查止损
            double returnRate = (currentPrice - avgPrice) / avgPrice;
            if (returnRate <= STOP_LOSS_THRESHOLD) {
                exitToQQQ(String.format("TQQQ跌幅(%.2f%%)触发止损", returnRate * 100));
                return true;
            }
        }

        // 检查SQQQ持仓
        if (tradeEngine.getQuantity(SQQQ) > 0) {
            List<Position> sqqqPositions = tradeEngine.getPositions(SQQQ);
            double avgHoldingDays = Position.getAverageHoldingDays(sqqqPositions, currentTime);
            double avgPrice = Position.getAveragePrice(sqqqPositions, currentTime);
            double currentPrice = tradeEngine.getPrice(SQQQ);

            // 检查持仓时间
            if (avgHoldingDays >= MAX_HOLDING_DAYS) {
                exitToQQQ(String.format("SQQQ平均持仓天数(%.2f)超过阈值", avgHoldingDays));
                return true;
            }

            // 检查止损
            double returnRate = (currentPrice - avgPrice) / avgPrice;
            if (returnRate <= STOP_LOSS_THRESHOLD) {
                exitToQQQ(String.format("SQQQ跌幅(%.2f%%)触发止损", returnRate * 100));
                return true;
            }
        }

        return false;
    }

    private void exitToQQQ(String reason) {
        if (tradeEngine.getQuantity(TQQQ) > 0) {
            tradeEngine.sell(TQQQ, PROPORTION_OF_100, reason);
        }
        if (tradeEngine.getQuantity(SQQQ) > 0) {
            tradeEngine.sell(SQQQ, PROPORTION_OF_100, reason);
        }
        tradeEngine.buy(QQQ, PROPORTION_OF_100, reason + "，转入QQQ");
    }

    private void dailyTrade(double rsi) {
        if (!tradeEngine.hasPosition()) {
            tradeEngine.buy(QQQ, PROPORTION_OF_100, "无持仓，买入QQQ");
            return;
        }

        if (rsi < EXTREME_OVERSOLD) {
            tradeEngine.sell(QQQ, PROPORTION_OF_100, String.format("RSI=%.2f 超卖，卖出QQQ换入TQQQ", rsi));
            tradeEngine.buy(TQQQ, PROPORTION_OF_100, String.format("RSI=%.2f 超卖，买入TQQQ", rsi));
            return;
        } else if (rsi > EXTREME_OVERBOUGHT) {
            tradeEngine.sell(QQQ, PROPORTION_OF_100, String.format("RSI=%.2f 超买，卖出QQQ换入SQQQ", rsi));
            tradeEngine.buy(SQQQ, PROPORTION_OF_100, String.format("RSI=%.2f 超买，买入SQQQ", rsi));
            return;
        }
        handleNormalHolding(rsi);
    }

    private void handleNormalHolding(double rsi) {
        // 持有SQQQ且RSI回归中性，换回QQQ
        if (tradeEngine.getQuantity(SQQQ) > 0 && rsi <= NEUTRAL_LEVEL) {
            tradeEngine.sell(SQQQ, PROPORTION_OF_100,
                             String.format("持有SQQQ，RSI=%.2f 回归中性，卖出SQQQ换回QQQ", rsi));
            tradeEngine.buy(QQQ, PROPORTION_OF_100,
                            String.format("RSI=%.2f 回归中性，买入QQQ", rsi));
        }
        // 持有TQQQ且RSI回归中性，换回QQQ
        else if (tradeEngine.getQuantity(TQQQ) > 0 && rsi >= NEUTRAL_LEVEL) {
            tradeEngine.sell(TQQQ, PROPORTION_OF_100,
                             String.format("持有TQQQ，RSI=%.2f 回归中性，卖出TQQQ换回QQQ", rsi));
            tradeEngine.buy(QQQ, PROPORTION_OF_100,
                            String.format("RSI=%.2f 回归中性，买入QQQ", rsi));
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        RSIChart chart = new RSIChart(klineMap, tradeDetail.getOrders());
        return chart.getCombinedChart();
    }
}