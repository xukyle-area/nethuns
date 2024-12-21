package com.gantenx.constant;

import com.gantenx.model.IndexPeriod;
import com.gantenx.model.IndexWeights;

public class Constants {
    public static final String ONE_DAY = "1d";
    public static final String MACD = "macd";
    public static final String BINANCE_URL = "https://data-api.binance.vision";
    public static final IndexPeriod INDEX_PERIOD = new IndexPeriod(6, 9, 6);
    public static final IndexWeights INDEX_WEIGHTS = new IndexWeights(0.5, 0.2, 0.3);
    public static final int PROPORTION_OF_100 = 100;
    public static final int PROPORTION_OF_75 = 75;
    public static final int PROPORTION_OF_50 = 50;
    public static final int PROPORTION_OF_25 = 25;
}
