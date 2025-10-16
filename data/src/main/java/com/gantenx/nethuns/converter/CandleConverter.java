package com.gantenx.nethuns.converter;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.model.Candle;


public class CandleConverter {

    // 返回转换后的 A/B 的 Kline 列表
    public static List<Candle> getCandle(Map<Long, Candle> baseMap, Map<Long, Candle> quoteMap, long start, long end) {
        List<Candle> resultList = new ArrayList<>();

        // 遍历时间戳区间
        for (long timestamp = start; timestamp <= end; timestamp += Period.D_1.getMillisecond()) {
            Candle baseKline = baseMap.get(timestamp); // A/USD Kline
            Candle quoteKline = quoteMap.get(timestamp); // B/USD Kline

            // 如果基准数据和目标数据都存在，则进行转换
            if (Objects.nonNull(baseKline) && Objects.nonNull(quoteKline)) {
                resultList.add(converter(baseKline, quoteKline));
            }
        }

        return resultList;
    }

    // 转换 A/USD 和 B/USD Kline 为 A/B Kline
    private static Candle converter(Candle base, Candle quote) {
        // 1. 时间戳保持一致
        Candle kline = new Candle(base.getTimestamp());

        // 2. 开盘价：A/USD 的开盘价 / B/USD 的开盘价
        double baseOpen = base.getOpen();
        double quoteOpen = quote.getOpen();
        kline.setOpen(baseOpen / quoteOpen);

        // 3. 最高价：A/USD 的最高价 / B/USD 的最高价
        double baseHigh = base.getHigh();
        double quoteHigh = quote.getHigh();
        kline.setHigh(baseHigh / quoteHigh);

        // 4. 最低价：A/USD 的最低价 / B/USD 的最低价
        double baseLow = base.getLow();
        double quoteLow = quote.getLow();
        kline.setLow(baseLow / quoteLow);

        // 5. 收盘价：A/USD 的收盘价 / B/USD 的收盘价
        double baseClose = base.getClose();
        double quoteClose = quote.getClose();
        kline.setClose(baseClose / quoteClose);

        // 6. 成交量：可以选择使用 base 或 quote 的成交量，这里我们选择 base 的成交量
        kline.setVolume(base.getVolume());

        return kline;
    }

    // 将 K 线数据转换为 KlineModel 对象
    public static List<Candle> kline(List<List<Object>> klinesData) {
        List<Candle> klines = new ArrayList<>();
        for (List<Object> kline : klinesData) {
            Candle model = new Candle((long) ((double) kline.get(0)));
            model.setOpen(Double.parseDouble((String) kline.get(1)));
            model.setHigh(Double.parseDouble((String) kline.get(2)));
            model.setLow(Double.parseDouble((String) kline.get(3)));
            model.setClose(Double.parseDouble((String) kline.get(4)));
            model.setVolume(Double.parseDouble((String) kline.get(5)));
            klines.add(model);
        }
        return klines;
    }

    public static <T> T nonOperation(T t) {
        return t;
    }
}
