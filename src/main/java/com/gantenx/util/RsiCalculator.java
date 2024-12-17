package com.gantenx.util;

import com.gantenx.model.KlineWithRSI;
import com.gantenx.model.Kline;

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
    public static List<KlineWithRSI> calculateAndAttachRSI(List<Kline> klineData, int period) {
        if (klineData.size() < period + 1) {
            throw new IllegalArgumentException("数据不足以计算 RSI");
        }

        // 提取收盘价
        List<Double> closePrices = new ArrayList<>();
        for (Kline kline : klineData) {
            closePrices.add(Double.parseDouble(kline.getClose()));
        }

        List<KlineWithRSI> klineWithRsiList = new ArrayList<>();
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
            KlineWithRSI klineWithRsi = new KlineWithRSI();
            copyKlineData(klineData.get(i), klineWithRsi);
            klineWithRsi.setRsi(null); // 前 period 天没有 RSI 值
            klineWithRsiList.add(klineWithRsi);
        }

        // 第一个 RSI
        double rs = avgGain / avgLoss;
        double firstRsi = 100 - (100 / (1 + rs));
        KlineWithRSI firstKlineWithRsiKline = new KlineWithRSI();
        copyKlineData(klineData.get(period), firstKlineWithRsiKline);
        firstKlineWithRsiKline.setRsi(firstRsi);
        klineWithRsiList.add(firstKlineWithRsiKline);

        // 按滑动窗口方式计算后续 RSI
        for (int i = period + 1; i < closePrices.size(); i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            double gain = Math.max(change, 0);
            double loss = Math.max(-change, 0);

            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;

            rs = avgGain / avgLoss;
            double rsiValue = 100 - (100 / (1 + rs));

            KlineWithRSI klineWithRsi = new KlineWithRSI();
            copyKlineData(klineData.get(i), klineWithRsi);
            klineWithRsi.setRsi(rsiValue);
            klineWithRsiList.add(klineWithRsi);
        }

        return klineWithRsiList;
    }

    /**
     * 复制 KlineModel 的基础数据到 RSI 对象
     */
    private static void copyKlineData(Kline source, KlineWithRSI target) {
        target.setTime(source.getTime());
        target.setOpen(source.getOpen());
        target.setHigh(source.getHigh());
        target.setLow(source.getLow());
        target.setClose(source.getClose());
        target.setVolume(source.getVolume());
    }
}

