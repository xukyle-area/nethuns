package com.gantenx.constant;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static com.gantenx.constant.Symbol.*;

public class Constants {
    private static final float LINE_STROKE_WIDTH = 2.0f;
    public static final BasicStroke BASE_STROKE = new BasicStroke(LINE_STROKE_WIDTH);
    public static final String TITLE = "Trading Chart";
    public static final Symbol CRYPTO_TRADING = ETHUSDT;
    public static final List<Symbol> CRYPTO_SYMBOL_LIST = Arrays.asList(DOGEUSDT, ETHUSDT, BTCUSDT);
    public static final List<Symbol> QQQ_SYMBOL_LIST = Arrays.asList(QQQUSD, SQQQUSD, TQQQUSD);


    public static final String MACD = "macd";
    public static final String BINANCE_URL = "https://data-api.binance.vision";
    public static final int PROPORTION_OF_100 = 100;
    public static final int PROPORTION_OF_95 = 95;
    public static final int PROPORTION_OF_90 = 90;
    public static final int PROPORTION_OF_85 = 85;
    public static final int PROPORTION_OF_80 = 80;
    public static final int PROPORTION_OF_75 = 75;
    public static final int PROPORTION_OF_70 = 70;
    public static final int PROPORTION_OF_65 = 65;
    public static final int PROPORTION_OF_60 = 60;
    public static final int PROPORTION_OF_55 = 55;
    public static final int PROPORTION_OF_50 = 50;
    public static final int PROPORTION_OF_45 = 45;
    public static final int PROPORTION_OF_40 = 40;
    public static final int PROPORTION_OF_35 = 35;
    public static final int PROPORTION_OF_30 = 30;
    public static final int PROPORTION_OF_25 = 25;
    public static final int PROPORTION_OF_20 = 20;
    public static final int PROPORTION_OF_15 = 15;
    public static final int PROPORTION_OF_10 = 10;
    public static final int PROPORTION_OF_5 = 5;

    public static final String TIME = "Time";
    public static final String PRICE = "Price";
    public static final int CHART_WIDTH = 2400;  // 增加宽度
    public static final int CHART_HEIGHT = 1200;

    public static final String TRADE_DETAIL = "trade-detail";
    public static final String ORDER_LIST = "order-list";
    public static final String RECORD_LIST = "record-list";
    public static final String PROFIT_LIST = "profit-list";
    public static final String LONG_HOLDING_PROFIT_RATE = "long-holding-profit-rate";
    public static final String RESULT = "result";
    public static final String LINES = "lines";
    public static final int RSI_PERIOD = 6;

    public static final double EPSILON = 1e-6;
    public static final double INITIAL_BALANCE = 100000;
    public final static double FEE = 0.001;
}
