package com.gantenx.nethuns.socket;


import com.gantenx.nethuns.commons.constant.Market;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketMain {

    public static void main(String[] args) {
        SocketTask socketTask = new SocketTask(Market.BINANCE);
        socketTask.scheduleConnect();
    }
}
