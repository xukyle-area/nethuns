package com.gantenx.nethuns.source.binance.socket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Event<T> {

    private String stream;

    @JsonProperty("data")
    private T data;
}
