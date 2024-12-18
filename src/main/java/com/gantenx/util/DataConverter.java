package com.gantenx.util;

import com.gantenx.model.Kline;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DataConverter {

    // 将 K 线数据转换为 KlineModel 对象
    public static List<Kline> convertToKlineModels(List<List<Object>> klinesData) {
        List<Kline> klines = new ArrayList<>();
        for (List<Object> kline : klinesData) {
            Kline model = new Kline((long) ((double) kline.get(0)));
            model.setOpen((String) kline.get(1));
            model.setHigh((String) kline.get(2));
            model.setLow((String) kline.get(3));
            model.setClose((String) kline.get(4));
            model.setVolume((String) kline.get(5));
            klines.add(model);
        }
        return klines;
    }
}
