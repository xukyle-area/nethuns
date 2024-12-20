package com.gantenx.strategy.qqq;

import com.gantenx.model.Kline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConsecutiveDays {
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

    public static ConsecutiveDays calculateConsecutiveDays(long timestamp, Map<Long, Kline> klineMap) {
        ConsecutiveDays days = new ConsecutiveDays();
        List<Long> sortedTimes = new ArrayList<>(klineMap.keySet());
        Collections.sort(sortedTimes);

        int index = sortedTimes.indexOf(timestamp);
        if (index <= 0) {
            return days;
        }

        double currentPrice = klineMap.get(timestamp).getClose();
        double previousPrice = klineMap.get(sortedTimes.get(index - 1)).getClose();

        // 判断当天是涨还是跌
        if (currentPrice > previousPrice) {
            // 向前查找连续上涨天数
            int gainDays = 1;
            for (int i = index - 1; i > 0; i--) {
                double price1 = klineMap.get(sortedTimes.get(i)).getClose();
                double price2 = klineMap.get(sortedTimes.get(i - 1)).getClose();
                if (price1 > price2) {
                    gainDays++;
                } else {
                    break;
                }
            }
            days.setGainDays(gainDays);
        } else if (currentPrice < previousPrice) {
            // 向前查找连续下跌天数
            int lossDays = 1;
            for (int i = index - 1; i > 0; i--) {
                double price1 = klineMap.get(sortedTimes.get(i)).getClose();
                double price2 = klineMap.get(sortedTimes.get(i - 1)).getClose();
                if (price1 < price2) {
                    lossDays++;
                } else {
                    break;
                }
            }
            days.setLossDays(lossDays);
        }
        return days;
    }
}