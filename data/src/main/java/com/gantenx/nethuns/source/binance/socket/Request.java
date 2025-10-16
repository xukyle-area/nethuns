package com.gantenx.nethuns.source.binance.socket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Request {

    private String method;

    private String[] params;

    private long id;

}
