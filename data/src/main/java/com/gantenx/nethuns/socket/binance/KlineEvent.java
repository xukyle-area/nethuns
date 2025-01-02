package com.gantenx.nethuns.socket.binance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KlineEvent {

    @JsonProperty("e")
    private String eventType; // 事件类型

    @JsonProperty("E")
    private long eventTime;   // 事件时间

    @JsonProperty("s")
    private String symbol;    // 交易对

    @JsonProperty("k")
    private Kline kline;  // K线数据

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Kline getKline() {
        return kline;
    }

    public void setKline(Kline kline) {
        this.kline = kline;
    }

    public static class Kline {

        @JsonProperty("t")
        private long startTime; // 这根K线的起始时间

        @JsonProperty("T")
        private long endTime;   // 这根K线的结束时间

        @JsonProperty("s")
        private String symbol;  // 交易对

        @JsonProperty("i")
        private String interval; // K线间隔

        @JsonProperty("f")
        private long firstTradeId; // 这根K线期间第一笔成交ID

        @JsonProperty("L")
        private long lastTradeId;  // 这根K线期间末一笔成交ID

        @JsonProperty("o")
        private String openPrice;  // 这根K线期间第一笔成交价

        @JsonProperty("c")
        private String closePrice; // 这根K线期间末一笔成交价

        @JsonProperty("h")
        private String highPrice;  // 这根K线期间最高成交价

        @JsonProperty("l")
        private String lowPrice;   // 这根K线期间最低成交价

        @JsonProperty("v")
        private String volume;     // 这根K线期间成交量

        @JsonProperty("n")
        private int tradeCount;    // 这根K线期间成交数量

        @JsonProperty("x")
        private boolean isClosed;  // 这根K线是否完结

        @JsonProperty("q")
        private String quoteVolume; // 这根K线期间成交额

        @JsonProperty("V")
        private String takerBuyVolume; // 主动买入的成交量

        @JsonProperty("Q")
        private String takerBuyQuoteVolume; // 主动买入的成交额

        @JsonProperty("B")
        private String ignored; // 忽略此参数

        // Getters and Setters
        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getInterval() {
            return interval;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }

        public long getFirstTradeId() {
            return firstTradeId;
        }

        public void setFirstTradeId(long firstTradeId) {
            this.firstTradeId = firstTradeId;
        }

        public long getLastTradeId() {
            return lastTradeId;
        }

        public void setLastTradeId(long lastTradeId) {
            this.lastTradeId = lastTradeId;
        }

        public String getOpenPrice() {
            return openPrice;
        }

        public void setOpenPrice(String openPrice) {
            this.openPrice = openPrice;
        }

        public String getClosePrice() {
            return closePrice;
        }

        public void setClosePrice(String closePrice) {
            this.closePrice = closePrice;
        }

        public String getHighPrice() {
            return highPrice;
        }

        public void setHighPrice(String highPrice) {
            this.highPrice = highPrice;
        }

        public String getLowPrice() {
            return lowPrice;
        }

        public void setLowPrice(String lowPrice) {
            this.lowPrice = lowPrice;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public int getTradeCount() {
            return tradeCount;
        }

        public void setTradeCount(int tradeCount) {
            this.tradeCount = tradeCount;
        }

        public boolean isClosed() {
            return isClosed;
        }

        public void setClosed(boolean closed) {
            isClosed = closed;
        }

        public String getQuoteVolume() {
            return quoteVolume;
        }

        public void setQuoteVolume(String quoteVolume) {
            this.quoteVolume = quoteVolume;
        }

        public String getTakerBuyVolume() {
            return takerBuyVolume;
        }

        public void setTakerBuyVolume(String takerBuyVolume) {
            this.takerBuyVolume = takerBuyVolume;
        }

        public String getTakerBuyQuoteVolume() {
            return takerBuyQuoteVolume;
        }

        public void setTakerBuyQuoteVolume(String takerBuyQuoteVolume) {
            this.takerBuyQuoteVolume = takerBuyQuoteVolume;
        }

        public String getIgnored() {
            return ignored;
        }

        public void setIgnored(String ignored) {
            this.ignored = ignored;
        }
    }
}

