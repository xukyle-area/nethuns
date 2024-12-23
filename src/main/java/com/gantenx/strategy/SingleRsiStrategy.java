package com.gantenx.strategy;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.constant.Period;
import com.gantenx.constant.Symbol;
import com.gantenx.strategy.template.SingleStrategy;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.PROPORTION_OF_100;
import static com.gantenx.constant.Constants.PROPORTION_OF_30;

@Slf4j
public class SingleRsiStrategy extends SingleStrategy {
    protected final Map<Long, Double> rsiMap;

    public SingleRsiStrategy(Symbol symbol, Period period, long start, long end) {
        super(SingleRsiStrategy.class.getSimpleName(), period, start, end, symbol);
        rsiMap = IndexTechnicalIndicators.calculateRSI(klineMap.get(symbol));
    }

    @Override
    protected void open() {
        while (tradeEngine.hasNext()) {
            long timestamp = tradeEngine.next();
            String dateStr = DateUtils.getDate(timestamp);
            Double RSI = rsiMap.get(timestamp);
            if (Objects.isNull(RSI)) {
                log.error("data not found, date:{}", dateStr);
                continue;
            }
            if (this.lowestOfDays(timestamp) > 5 && RSI < 50) {
                tradeEngine.buy(symbol, PROPORTION_OF_30, "rsi: " + String.format("%.2f", RSI));
            } else if (tradeEngine.getQuantity(symbol) > 0 && RSI >= 60) {
                tradeEngine.sell(symbol, PROPORTION_OF_100, "rsi: " + String.format("%.2f", RSI));
            }
        }
    }

    public int lowestOfDays(long timestamp) {
        Double curRsi = rsiMap.get(timestamp);
        if (curRsi == null) {
            throw new IllegalArgumentException("No RSI data for the given timestamp: " + timestamp);
        }

        int days = 0;
        while (true) {
            long previousTimestamp = timestamp - (++days) * Period.ONE_DAY.getMillisecond();
            Double previousRsi = rsiMap.get(timestamp);
            if (previousRsi == null || curRsi >= previousRsi) {
                return days - 1;
            }
        }
    }
}
