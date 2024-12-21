package com.gantenx.engine;

import com.gantenx.model.Time;
import com.gantenx.strategy.qqq.ConsecutiveDays;

public class TradingContext extends Time {
    private double rsi;
    private double previousRsi;
    private double ema;
    private double fastEma;
    private double macd;
    private double qqqPrice;
    private double tqqqPrice;
    private double sqqqPrice;
    private int consecutiveGainDays;
    private int consecutiveLossDays;

    private TradingContext(long timestamp) {
        super(timestamp);
    }

    public void setRsi(double rsi) {
        this.rsi = rsi;
    }

    public void setPreviousRsi(double previousRsi) {
        this.previousRsi = previousRsi;
    }

    public void setEma(double ema) {
        this.ema = ema;
    }

    public void setFastEma(double fastEma) {
        this.fastEma = fastEma;
    }

    public void setMacd(double macd) {
        this.macd = macd;
    }

    public void setQqqPrice(double qqqPrice) {
        this.qqqPrice = qqqPrice;
    }

    public void setTqqqPrice(double tqqqPrice) {
        this.tqqqPrice = tqqqPrice;
    }

    public void setSqqqPrice(double sqqqPrice) {
        this.sqqqPrice = sqqqPrice;
    }

    public void setConsecutiveGainDays(int consecutiveGainDays) {
        this.consecutiveGainDays = consecutiveGainDays;
    }

    public void setConsecutiveLossDays(int consecutiveLossDays) {
        this.consecutiveLossDays = consecutiveLossDays;
    }

    public double getRsi() {
        return rsi;
    }

    public double getPreviousRsi() {
        return previousRsi;
    }

    public double getEma() {
        return ema;
    }

    public double getFastEma() {
        return fastEma;
    }

    public double getMacd() {
        return macd;
    }

    public double getQqqPrice() {
        return qqqPrice;
    }

    public double getTqqqPrice() {
        return tqqqPrice;
    }

    public double getSqqqPrice() {
        return sqqqPrice;
    }

    public int getConsecutiveGainDays() {
        return consecutiveGainDays;
    }

    public int getConsecutiveLossDays() {
        return consecutiveLossDays;
    }

    public static boolean confirmDowntrendReversal(TradingContext context) {
        // 1. 价格跌破EMA
        boolean priceBelowEMA = context.getQqqPrice() < context.getEma();

        // 2. 短期EMA向下突破长期EMA
        boolean emaCrossdown = context.getFastEma() < context.getEma();

        // 3. MACD确认
        boolean macdNegative = context.getMacd() < 0;

        // 4. 连续下跌天数确认
        boolean consecutiveLosses = context.getConsecutiveLossDays() >= 2;

        // 5. RSI动量确认
        boolean rsiWeakening = context.getRsi() < context.getPreviousRsi();

        // 需要满足至少3个条件
        int confirmationCount = 0;
        if (priceBelowEMA) confirmationCount++;
        if (emaCrossdown) confirmationCount++;
        if (macdNegative) confirmationCount++;
        if (consecutiveLosses) confirmationCount++;
        if (rsiWeakening) confirmationCount++;

        return confirmationCount >= 3;
    }

    public static boolean confirmUptrendReversal(TradingContext context) {
        // 1. 价格突破EMA
        boolean priceAboveEMA = context.getQqqPrice() > context.getEma();

        // 2. 短期EMA向上突破长期EMA
        boolean emaCrossover = context.getFastEma() > context.getEma();

        // 3. MACD确认
        boolean macdPositive = context.getMacd() > 0;

        // 4. 连续上涨天数确认
        boolean consecutiveGains = context.getConsecutiveGainDays() >= 2;

        // 5. RSI动量确认
        boolean rsiMomentum = context.getRsi() > context.getPreviousRsi();

        // 需要满足至少3个条件
        int confirmationCount = 0;
        if (priceAboveEMA) confirmationCount++;
        if (emaCrossover) confirmationCount++;
        if (macdPositive) confirmationCount++;
        if (consecutiveGains) confirmationCount++;
        if (rsiMomentum) confirmationCount++;

        return confirmationCount >= 3;
    }

    public static TradingContext buildTradingContext(long ts,
                                                     Double rsi,
                                                     double qqqPrice,
                                                     double tqqqPrice,
                                                     double sqqqPrice,
                                                     double ema,
                                                     double fastEma,
                                                     double macd,
                                                     double previousRsi,
                                                     ConsecutiveDays consecutiveDays) {
        TradingContext context = new TradingContext(ts);
        context.setPreviousRsi(rsi);
        context.setEma(ema);
        context.setFastEma(fastEma);
        context.setMacd(macd);
        context.setQqqPrice(qqqPrice);
        context.setTqqqPrice(tqqqPrice);
        context.setSqqqPrice(sqqqPrice);
        context.setConsecutiveGainDays(consecutiveDays.getGainDays());
        context.setConsecutiveLossDays(consecutiveDays.getLossDays());
        context.setPreviousRsi(previousRsi);
        return context;
    }

}