package com.gantenx;

import com.gantenx.model.Kline;
import com.gantenx.service.KlineService;
import com.gantenx.trend.PriceTrendIdentifier;
import com.gantenx.trend.TrendIdentifier;
import com.gantenx.utils.DateUtils;

import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.CRYPTO_TRADING;
import static com.gantenx.constant.Period.ONE_DAY;

public class TestMain {
    private static final TrendIdentifier identifier = new PriceTrendIdentifier();

    public static void main(String[] args) {
        long start = DateUtils.getTimestamp("20240101");
        long end = DateUtils.getTimestamp("20241220");
        Map<Long, Kline> klineMap = KlineService.getKLineMap(CRYPTO_TRADING, ONE_DAY, start, end);
        long watchStart = DateUtils.getTimestamp("20240301");
        long watchEnd = DateUtils.getTimestamp("20240801");
        List<Long> timestampList = DateUtils.genTimeList(ONE_DAY, watchStart, watchEnd);
        identifier.identify(klineMap, timestampList, 3);
    }


}
