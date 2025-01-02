package com.gantenx.nethuns.socket;

import com.gantenx.nethuns.commons.constant.Market;
import com.gantenx.nethuns.commons.constant.Symbol;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
public class RefreshTask {

    public static void run(Consumer<String> send, Market market) {
        try {
            String sub = SubscriptionUtils.ofTickerSubscription(market, Collections.singleton(Symbol.BTCUSDT));
            send.accept(sub);
        } catch (Throwable e) {
            log.error("send subscription exception, market:{}", market, e);
        }
    }
}
