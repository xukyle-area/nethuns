package com.gantenx.nethuns.socket.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Event<T> {

    private String stream;

    @JsonProperty("data")
    private T data;
}
