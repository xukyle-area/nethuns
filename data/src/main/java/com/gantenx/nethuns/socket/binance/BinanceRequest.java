package com.gantenx.nethuns.socket.binance;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BinanceRequest {

    private String method;

    private String[] params;

    private long id;

}
