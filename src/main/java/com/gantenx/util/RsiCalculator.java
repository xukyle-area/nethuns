package com.gantenx.util;

import com.gantenx.model.RSI;
import com.gantenx.model.response.KlineModel;

import java.util.ArrayList;
import java.util.List;

public class RsiCalculator {

    /**
     * 计算 RSI 并将结果组装到 RSI 对象中
     *
     * @param klineData K线数据列表
     * @param period    RSI 的计算周期
     * @return 包含 RSI 的扩展 K 线数据列表
     */
    public static List<RSI> calculateAndAttachRSI(List<KlineModel> klineData, int period) {
        if (klineData.size() < period + 1) {
            throw new IllegalArgumentException("数据不足以计算 RSI");
        }

        // 提取收盘价
        List<Double> closePrices = new ArrayList<>();
        for (KlineModel kline : klineData) {
            closePrices.add(Double.parseDouble(kline.getClosePrice()));
        }

        List<RSI> rsiList = new ArrayList<>();
        double avgGain = 0.0;
        double avgLoss = 0.0;

        // 初始化计算第一个平均涨幅和跌幅
        for (int i = 1; i <= period; i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss -= change;
            }
        }
        avgGain /= period;
        avgLoss /= period;

        // 添加前 `period` 天的 K 线数据（没有 RSI 值）
        for (int i = 0; i < period; i++) {
            RSI rsi = new RSI();
            copyKlineData(klineData.get(i), rsi);
            rsi.setRsi(null); // 前 period 天没有 RSI 值
            rsiList.add(rsi);
        }

        // 第一个 RSI
        double rs = avgGain / avgLoss;
        double firstRsi = 100 - (100 / (1 + rs));
        RSI firstRsiKline = new RSI();
        copyKlineData(klineData.get(period), firstRsiKline);
        firstRsiKline.setRsi(firstRsi);
        rsiList.add(firstRsiKline);

        // 按滑动窗口方式计算后续 RSI
        for (int i = period + 1; i < closePrices.size(); i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            double gain = Math.max(change, 0);
            double loss = Math.max(-change, 0);

            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;

            rs = avgGain / avgLoss;
            double rsiValue = 100 - (100 / (1 + rs));

            RSI rsi = new RSI();
            copyKlineData(klineData.get(i), rsi);
            rsi.setRsi(rsiValue);
            rsiList.add(rsi);
        }

        return rsiList;
    }

    /**
     * 复制 KlineModel 的基础数据到 RSI 对象
     */
    private static void copyKlineData(KlineModel source, RSI target) {
        target.setOpenTime(source.getOpenTime());
        target.setOpenPrice(source.getOpenPrice());
        target.setHighPrice(source.getHighPrice());
        target.setLowPrice(source.getLowPrice());
        target.setClosePrice(source.getClosePrice());
        target.setVolume(source.getVolume());
        target.setCloseTime(source.getCloseTime());
        target.setQuoteAssetVolume(source.getQuoteAssetVolume());
        target.setNumberOfTrades(source.getNumberOfTrades());
        target.setTakerBuyBaseAssetVolume(source.getTakerBuyBaseAssetVolume());
        target.setTakerBuyQuoteAssetVolume(source.getTakerBuyQuoteAssetVolume());
    }
}

