package com.gantenx.nethuns.engine.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.commons.utils.DateUtils;

/**
 * K线价格提供器
 * 基于K线数据提供价格信息
 */
public class KlinePriceProvider implements PriceProvider {

    private final Map<Symbol, Map<Long, Candle>> klineMap;

    /**
     * 构造器
     *
     * @param klineMap K线数据映射
     */
    public KlinePriceProvider(Map<Symbol, Map<Long, Candle>> klineMap) {
        validateKlineMap(klineMap);
        // 创建防御性副本
        this.klineMap = Collections.unmodifiableMap(deepCopyKlineMap(klineMap));
    }

    @Override
    public double getPrice(Symbol symbol, long timestamp) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }

        Map<Long, Candle> symbolKlines = klineMap.get(symbol);
        if (symbolKlines == null) {
            throw new IllegalArgumentException("No kline data for symbol: " + symbol);
        }

        Candle candle = symbolKlines.get(timestamp);
        if (candle == null) {
            throw new IllegalArgumentException(String.format("No kline data for symbol: %s at timestamp: %s", symbol,
                    DateUtils.getDate(timestamp)));
        }

        return candle.getOpen();
    }

    @Override
    public boolean hasPrice(Symbol symbol) {
        if (symbol == null) {
            return false;
        }
        Map<Long, Candle> symbolKlines = klineMap.get(symbol);
        return symbolKlines != null && !symbolKlines.isEmpty();
    }

    @Override
    public boolean hasPrice(Symbol symbol, long timestamp) {
        if (symbol == null) {
            return false;
        }
        Map<Long, Candle> symbolKlines = klineMap.get(symbol);
        return symbolKlines != null && symbolKlines.containsKey(timestamp);
    }

    /**
     * 获取支持的交易标的
     *
     * @return 支持的交易标的集合
     */
    public java.util.Set<Symbol> getSupportedSymbols() {
        return klineMap.keySet();
    }

    /**
     * 获取指定标的的所有时间戳
     *
     * @param symbol 交易标的
     * @return 时间戳集合，如果没有数据则返回空集合
     */
    public java.util.Set<Long> getAvailableTimestamps(Symbol symbol) {
        Map<Long, Candle> symbolKlines = klineMap.get(symbol);
        return symbolKlines != null ? symbolKlines.keySet() : Collections.emptySet();
    }

    /**
     * 验证K线数据映射
     */
    private void validateKlineMap(Map<Symbol, Map<Long, Candle>> klineMap) {
        if (klineMap == null) {
            throw new IllegalArgumentException("Kline map cannot be null");
        }
        if (klineMap.isEmpty()) {
            throw new IllegalArgumentException("Kline map cannot be empty");
        }

        // 验证每个标的都有数据
        for (Map.Entry<Symbol, Map<Long, Candle>> entry : klineMap.entrySet()) {
            Symbol symbol = entry.getKey();
            Map<Long, Candle> symbolData = entry.getValue();

            if (symbol == null) {
                throw new IllegalArgumentException("Symbol cannot be null in kline map");
            }
            if (symbolData == null || symbolData.isEmpty()) {
                throw new IllegalArgumentException("Kline data cannot be null or empty for symbol: " + symbol);
            }

            // 验证K线数据
            for (Map.Entry<Long, Candle> candleEntry : symbolData.entrySet()) {
                Long timestamp = candleEntry.getKey();
                Candle candle = candleEntry.getValue();

                if (timestamp == null || timestamp <= 0) {
                    throw new IllegalArgumentException("Invalid timestamp in kline data for symbol: " + symbol);
                }
                if (candle == null) {
                    throw new IllegalArgumentException(
                            "Candle cannot be null for symbol: " + symbol + " at timestamp: " + timestamp);
                }
            }
        }
    }

    /**
     * 创建K线数据的深拷贝
     */
    private Map<Symbol, Map<Long, Candle>> deepCopyKlineMap(Map<Symbol, Map<Long, Candle>> original) {
        Map<Symbol, Map<Long, Candle>> copy = new HashMap<>();
        for (Map.Entry<Symbol, Map<Long, Candle>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        return copy;
    }
}
