package com.gantenx.nethuns.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gantenx.nethuns.commons.constant.Market;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.socket.binance.BinanceRequest;
import com.gantenx.nethuns.socket.cryptocom.CryptoRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public final class SubscriptionUtils {

    private SubscriptionUtils() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    private final static String BINANCE_SUBSCRIBE = "SUBSCRIBE";
    private final static String CRYPTO_COM_SUBSCRIBE = "subscribe";
    private final static String CRYPTO_CHANNELS = "channels";
    private static long id = 1L;

    public static String ofTickerSubscription(Market market, Set<Symbol> symbols) throws JsonProcessingException {
        log.info("build TickerSubscription market:{}, symbols:{}", market, symbols);
        if (market == Market.BINANCE) {
            List<String> params = SubscriptionUtils.buildParamsForBinance(symbols);
            BinanceRequest request = new BinanceRequest(BINANCE_SUBSCRIBE, params.toArray(new String[0]), id++);
            return mapper.writeValueAsString(request);
        } else if (market == Market.CRYPTO) {
            List<String> channels = SubscriptionUtils.buildParamForCrypto(symbols);
            Map<String, Object> channelsMap = Collections.singletonMap(CRYPTO_CHANNELS, channels);
            CryptoRequest request = new CryptoRequest(id++,
                                                      CRYPTO_COM_SUBSCRIBE,
                                                      channelsMap,
                                                      System.currentTimeMillis());
            return mapper.writeValueAsString(request);
        } else {
            throw new RuntimeException("cannot convert the ticker subscription");
        }
    }

    private static List<String> buildParamsForBinance(Set<Symbol> symbols) {
        ArrayList<String> list = new ArrayList<>();
        for (Symbol s : symbols) {
            String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
            String x = symbol.toUpperCase() + "@ticker";
            list.add(x.toLowerCase());
        }
        return list;
    }

    private static List<String> buildParamForCrypto(Set<Symbol> symbols) {
        ArrayList<String> list = new ArrayList<>();
        for (Symbol s : symbols) {
            String x = "ticker." + s.getBase().getUpperName() + "_" + s.getQuote().getUpperName();
            list.add(x);
        }
        return list;
    }
}
