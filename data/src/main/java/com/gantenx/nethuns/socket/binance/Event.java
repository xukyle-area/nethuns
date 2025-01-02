package com.gantenx.nethuns.socket.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Event<T> {

    private String stream;

    @JsonProperty("data")
    private T data;
}
