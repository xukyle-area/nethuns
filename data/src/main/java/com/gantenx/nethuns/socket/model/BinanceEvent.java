package com.gantenx.nethuns.socket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BinanceEvent {

    private String stream;

    @JsonProperty("data")
    private BinanceTicker data;

}
