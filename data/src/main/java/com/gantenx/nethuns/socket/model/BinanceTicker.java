package com.gantenx.nethuns.socket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BinanceTicker {

    @JsonProperty("e")
    private String eventType;

    @JsonProperty("E")
    private long eventTime;

    @JsonProperty("s")
    private String symbol;

    @JsonProperty("p")
    private String priceChange;

    @JsonProperty("P")
    private String priceChangePercent;

    @JsonProperty("w")
    private String weightedAveragePrice;

    @JsonProperty("x")
    private String previousDaysClosePrice;

    @JsonProperty("Q")
    private String closeTradesQuantity;

    @JsonProperty("b")
    private String bestBidPrice;

    @JsonProperty("B")
    private String bestBidQuantity;

    @JsonProperty("a")
    private String bestAskPrice;

    @JsonProperty("c")
    private String lastTradedPrice;

    @JsonProperty("A")
    private String bestAskQuantity;

    @JsonProperty("o")
    private String openPrice;

    @JsonProperty("h")
    private String highPrice;

    @JsonProperty("l")
    private String lowPrice;

    @JsonProperty("v")
    private String totalTradedBaseAssetVolume;

    @JsonProperty("q")
    private String totalTradedQuoteAssetVolume;

    @JsonProperty("O")
    private long statisticsOpenTime;

    @JsonProperty("C")
    private long statisticsCloseTime;

    @JsonProperty("F")
    private long firstTradeId;

    @JsonProperty("L")
    private long lastTradeId;

    @JsonProperty("n")
    private long totalNumberOfTrades;

}
