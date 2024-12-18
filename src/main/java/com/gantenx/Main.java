package com.gantenx;

import com.gantenx.strategy.QQQStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        QQQStrategy.replay("20211112", "20221014");
    }
}
