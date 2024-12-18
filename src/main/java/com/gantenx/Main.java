package com.gantenx;

import com.gantenx.model.Kline;
import com.gantenx.strategy.QQQStrategy;
import com.gantenx.util.CsvUtils;
import com.gantenx.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Main {
    public static void main(String[] args) {
        // QQQStrategy.replay("20230106", "20241210");

        List<Kline> sqqqKlineList = CsvUtils.getKLineFromCsv("data/SQQQ.csv", "20230106", "20241210");
        for (Kline kline : sqqqKlineList) {
            log.info(JsonUtils.toJson(kline));
        }
    }
}
