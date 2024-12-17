package com.gantenx.model.response;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DataConverter {

    // 将 K 线数据转换为 KlineModel 对象
    public static List<KlineModel> convertToKlineModels(List<List<Object>> klinesData) {
        List<KlineModel> klineModels = new ArrayList<>();
        for (List<Object> kline : klinesData) {
            KlineModel model = new KlineModel();
            model.setOpenTime((long) ((double) kline.get(0)));
            model.setOpenPrice((String) kline.get(1));
            model.setHighPrice((String) kline.get(2));
            model.setLowPrice((String) kline.get(3));
            model.setClosePrice((String) kline.get(4));
            model.setVolume((String) kline.get(5));
            model.setCloseTime((long) ((double) kline.get(6)));
            model.setQuoteAssetVolume((String) kline.get(7));
            model.setNumberOfTrades((int) ((double) kline.get(8)));
            model.setTakerBuyBaseAssetVolume((String) kline.get(9));
            model.setTakerBuyQuoteAssetVolume((String) kline.get(10));
            klineModels.add(model);
        }
        return klineModels;
    }
}
