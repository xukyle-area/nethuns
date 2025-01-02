package com.gantenx.nethuns.socket.binance;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Request {

    private String method;

    private String[] params;

    private long id;

}
