package com.gantenx.nethuns.commons.constant;


import com.google.common.base.Joiner;

import java.awt.*;

import static com.gantenx.nethuns.commons.constant.Symbol.ETHUSDT;


public class Constants {
    private static final float LINE_STROKE_WIDTH = 2.0f;
    public static final BasicStroke BASE_STROKE = new BasicStroke(LINE_STROKE_WIDTH);
    public static final String TITLE = "Trading Chart";
    public static final String BINANCE_URL = "https://data-api.binance.vision";
    public static final String TIME = "Time";
    public static final String CANDLE = "Candle";
    public static final String PRICE = "Price";
    public static final String DATA = "Data";
    public static final int CHART_WIDTH = 2400;
    public static final int CHART_HEIGHT = 1200;
    public static final String TRADE_DETAIL = "trade-detail";
    public static final String ORDER_LIST = "order-list";
    public static final String RECORD_LIST = "record-list";
    public static final String LONG_HOLDING_PROFIT_RATE = "long-holding-profit-rate";
    public static final String RESULT = "result";
    public static final String LINES = "lines";
    public static final int RSI_PERIOD = 6;
    public static final double EPSILON = 1e-6;
    public static final double INITIAL_BALANCE = 10000;
    public final static double FEE = 0.001;
    public static final Joiner joiner = Joiner.on("/");
}
